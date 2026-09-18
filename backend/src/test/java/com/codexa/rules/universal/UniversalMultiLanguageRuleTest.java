package com.codexa.rules.universal;

import com.codexa.analysis.pipeline.PipelineContext;
import com.codexa.rules.api.RuleContext;
import com.codexa.rules.api.RuleFinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UniversalMultiLanguageRuleTest {

    private UniversalMultiLanguageRule rule;

    @BeforeEach
    void setUp() {
        rule = new UniversalMultiLanguageRule();
    }

    @Test
    void testHardcodedAdminAuthDetected(@TempDir Path tempDir) throws IOException {
        Path srcDir = tempDir.resolve("src/components");
        Files.createDirectories(srcDir);
        Path file = srcDir.resolve("SuperAdminLoginView.tsx");
        Files.writeString(file, """
                import React from 'react';
                export const SuperAdminLoginView = () => {
                    const handleLogin = (adminId: string, password: string) => {
                        if (adminId === 'admin' && password === 'admin123') {
                            sessionStorage.setItem('isAdmin', 'true');
                        }
                    };
                    return <div>Login</div>;
                };
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean found = findings.stream().anyMatch(f -> "CR-AUTH-002".equals(f.ruleId()));
        assertTrue(found, "Should detect CR-AUTH-002 hardcoded admin authentication");
    }

    @Test
    void testClientSideSecretLeakDetected(@TempDir Path tempDir) throws IOException {
        Path srcDir = tempDir.resolve("src/services");
        Files.createDirectories(srcDir);
        Path emailService = srcDir.resolve("emailService.ts");
        Files.writeString(emailService, """
                const mailtrapToken = import.meta.env.VITE_MAILTRAP_API_TOKEN;
                export const sendEmail = async () => {};
                """);

        Path aiService = srcDir.resolve("aiSurveyService.ts");
        Files.writeString(aiService, """
                const geminiKey = import.meta.env.VITE_GEMINI_API_KEY;
                export const analyze = async () => {};
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        List<RuleFinding> leaks = findings.stream().filter(f -> "CR-LEAK-001".equals(f.ruleId())).toList();
        assertEquals(2, leaks.size(), "Should detect 2 client secret leaks (CR-LEAK-001)");
    }

    @Test
    void testDevMiddlewareApiRouteDetected(@TempDir Path tempDir) throws IOException {
        Path viteConfig = tempDir.resolve("vite.config.ts");
        Files.writeString(viteConfig, """
                import { defineConfig } from 'vite';
                export default defineConfig({
                    plugins: [{
                        name: 'dev-api',
                        configureServer(server) {
                            server.middlewares.use((req, res, next) => {
                                if (req.url === '/api/email' && req.method === 'POST') {
                                    res.end('ok');
                                } else {
                                    next();
                                }
                            });
                        }
                    }]
                });
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean found = findings.stream().anyMatch(f -> "CR-ARCH-001".equals(f.ruleId()));
        assertTrue(found, "Should detect CR-ARCH-001 production API defined in dev middleware");
    }

    @Test
    void testPredictablePinAndTimestampTokenDetected(@TempDir Path tempDir) throws IOException {
        Path storeDir = tempDir.resolve("src/store");
        Files.createDirectories(storeDir);
        Path storeFile = storeDir.resolve("smartLotStore.ts");
        Files.writeString(storeFile, """
                export const createScheme = (payload: any) => {
                    const randomPin = Math.floor(1000 + Math.random() * 9000).toString();
                    const token = `tok_${payload.schemeId.toLowerCase()}_${Date.now()}`;
                    return { randomPin, token };
                };
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean foundPin = findings.stream().anyMatch(f -> "CR-RAND-001".equals(f.ruleId()));
        boolean foundToken = findings.stream().anyMatch(f -> "CR-RAND-002".equals(f.ruleId()));
        assertTrue(foundPin, "Should detect CR-RAND-001 for Math.random() PIN");
        assertTrue(foundToken, "Should detect CR-RAND-002 for Date.now() timestamp token");
    }

    @Test
    void testSupabaseEdgeFunctionOpenRelayDetected(@TempDir Path tempDir) throws IOException {
        Path funcDir = tempDir.resolve("supabase/functions/send-activity-email");
        Files.createDirectories(funcDir);
        Path funcFile = funcDir.resolve("index.ts");
        Files.writeString(funcFile, """
                import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
                serve(async (req) => {
                    const { email, message } = await req.json();
                    await fetch("https://api.mailtrap.io/v1/send", { method: "POST" });
                    return new Response("Sent");
                });
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean found = findings.stream().anyMatch(f -> "CR-EDGE-001".equals(f.ruleId()));
        assertTrue(found, "Should detect CR-EDGE-001 for unauthenticated Supabase Edge Function open relay");
    }

    @Test
    void testSupabaseEdgeFunctionWithAuthNotFlagged(@TempDir Path tempDir) throws IOException {
        Path funcDir = tempDir.resolve("supabase/functions/safe-function");
        Files.createDirectories(funcDir);
        Path funcFile = funcDir.resolve("index.ts");
        Files.writeString(funcFile, """
                import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
                serve(async (req) => {
                    const authHeader = req.headers.get("Authorization");
                    if (!authHeader) return new Response("Unauthorized", { status: 401 });
                    const { data: { user } } = await supabaseClient.auth.getUser(authHeader.replace("Bearer ", ""));
                    if (!user) return new Response("Unauthorized", { status: 401 });
                    return new Response("OK");
                });
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean found = findings.stream().anyMatch(f -> "CR-EDGE-001".equals(f.ruleId()));
        assertFalse(found, "Safe edge function with Authorization verification must not trigger CR-EDGE-001");
    }

    @Test
    void testHardcodedJwtAndInsecureRlsDetected(@TempDir Path tempDir) throws IOException {
        Path scriptFile = tempDir.resolve("seed.mjs");
        Files.writeString(scriptFile, """
                import { createClient } from '@supabase/supabase-js';
                const supabase = createClient('https://example.supabase.co', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBpZXBsbXBrb2duYmRrdGV6dGViIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjY2NTQ4NDcsImV4cCI6MjA0MjIzMDg0N30.8V4Xw9-abcdef');
                """);

        Path sqlFile = tempDir.resolve("disable_rls.sql");
        Files.writeString(sqlFile, """
                ALTER TABLE members DISABLE ROW LEVEL SECURITY;
                CREATE POLICY "Allow all" ON schemes FOR ALL USING (true);
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean foundJwt = findings.stream().anyMatch(f -> "CR-SEC-013".equals(f.ruleId()));
        boolean foundRls = findings.stream().anyMatch(f -> "CR-RLS-001".equals(f.ruleId()));
        assertTrue(foundJwt, "Should detect CR-SEC-013 hardcoded JWT in seed.mjs");
        assertTrue(foundRls, "Should detect CR-RLS-001 insecure RLS disable in disable_rls.sql");
    }
}
