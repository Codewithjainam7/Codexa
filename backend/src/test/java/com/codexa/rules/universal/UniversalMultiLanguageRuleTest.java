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

    @Test
    void testUtf16LeEncodedDisableRlsDetected(@TempDir Path tempDir) throws IOException {
        String sql = """
                CREATE POLICY "Allow anon access" ON public.schemes FOR ALL USING (true);
                CREATE POLICY "Allow anon access" ON public.members FOR ALL USING (true);
                CREATE POLICY "Allow anon access" ON public.units FOR ALL USING (true);
                CREATE POLICY "Allow anon access" ON public.resident_requests FOR ALL USING (true);
                CREATE POLICY "Allow anon access" ON public.profiles FOR ALL USING (true);
                """;
        Path sqlFile = tempDir.resolve("disable_rls.sql");
        byte[] contentBytes = sql.getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
        byte[] withBom = new byte[contentBytes.length + 2];
        withBom[0] = (byte) 0xFF;
        withBom[1] = (byte) 0xFE;
        System.arraycopy(contentBytes, 0, withBom, 2, contentBytes.length);
        Files.write(sqlFile, withBom);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        List<RuleFinding> rlsFindings = findings.stream().filter(f -> "CR-RLS-001".equals(f.ruleId())).toList();
        assertFalse(rlsFindings.isEmpty(), "Must detect CR-RLS-001 in UTF-16LE encoded SQL files");
    }

    @Test
    void testViteConfigFallbackTokenAndDevApiDetected(@TempDir Path tempDir) throws IOException {
        Path viteConfig = tempDir.resolve("vite.config.ts");
        Files.writeString(viteConfig, """
                import { defineConfig } from 'vite';
                export default defineConfig({
                    plugins: [{
                        name: 'dev-api',
                        configureServer(server) {
                            const token = process.env.MAILTRAP_API_TOKEN || 'b68d42639db12dd9c3a52f87968d94de';
                            const inboxId = process.env.MAILTRAP_INBOX_ID || '4900976';
                            server.middlewares.use('/api/email', async (req, res) => {
                                res.end('ok');
                            });
                        }
                    }]
                });
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean foundToken = findings.stream().anyMatch(f -> "CR-SEC-002".equals(f.ruleId()));
        boolean foundDevApi = findings.stream().anyMatch(f -> "CR-ARCH-001".equals(f.ruleId()));
        assertTrue(foundToken, "Must detect CR-SEC-002 for process.env.MAILTRAP_API_TOKEN / MAILTRAP_INBOX_ID fallback");
        assertTrue(foundDevApi, "Must detect CR-ARCH-001 for dev middleware API route");
    }

    @Test
    void testSupabaseEdgeFunctionWithCorsHeadersStillFlaggedAsOpenRelay(@TempDir Path tempDir) throws IOException {
        Path funcDir = tempDir.resolve("supabase/functions/send-activity-email");
        Files.createDirectories(funcDir);
        Path funcFile = funcDir.resolve("index.ts");
        Files.writeString(funcFile, """
                import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
                const corsHeaders = {
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
                };
                serve(async (req) => {
                    const { email, message } = await req.json();
                    await fetch("https://api.mailtrap.io/v1/send", { method: "POST" });
                    return new Response("Sent", { headers: corsHeaders });
                });
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean found = findings.stream().anyMatch(f -> "CR-EDGE-001".equals(f.ruleId()));
        assertTrue(found, "Edge function with authorization header in CORS must still be flagged if no real auth check is performed");
    }

    @Test
    void testMultiLineSqlMigrationRlsBypassDetected(@TempDir Path tempDir) throws IOException {
        Path migDir = tempDir.resolve("supabase/migrations");
        Files.createDirectories(migDir);
        Path surveyMig = migDir.resolve("20260917_surveys_and_responses.sql");
        Files.writeString(surveyMig, """
                ALTER TABLE public.surveys ENABLE ROW LEVEL SECURITY;
                
                DROP POLICY IF EXISTS "Public and auth full access surveys" ON public.surveys;
                CREATE POLICY "Public and auth full access surveys" ON public.surveys
                  FOR ALL TO anon, authenticated
                  USING (true)
                  WITH CHECK (true);
                  
                DROP POLICY IF EXISTS "Public and auth full access survey_responses" ON public.survey_responses;
                CREATE POLICY "Public and auth full access survey_responses" ON public.survey_responses
                  FOR ALL TO anon, authenticated
                  USING (true)
                  WITH CHECK (true);
                """);

        Path permMig = migDir.resolve("20260831_permissions_database_sync.sql");
        Files.writeString(permMig, """
                ALTER TABLE public.role_permissions ENABLE ROW LEVEL SECURITY;
                
                DROP POLICY IF EXISTS "Allow authenticated read role_permissions" ON public.role_permissions;
                CREATE POLICY "Allow authenticated read role_permissions" 
                ON public.role_permissions FOR SELECT TO authenticated USING (true);
                
                DROP POLICY IF EXISTS "Allow authenticated insert/update role_permissions" ON public.role_permissions;
                CREATE POLICY "Allow authenticated insert/update role_permissions" 
                ON public.role_permissions FOR ALL TO authenticated USING (true) WITH CHECK (true);
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        List<RuleFinding> rlsFindings = findings.stream().filter(f -> "CR-RLS-001".equals(f.ruleId())).toList();
        assertTrue(rlsFindings.size() >= 4, "Should detect at least 4 CR-RLS-001 findings across both multi-line migration scripts (actual: " + rlsFindings.size() + ")");
        
        boolean foundSurveyMig = rlsFindings.stream().anyMatch(f -> f.filePath().contains("20260917_surveys_and_responses.sql"));
        boolean foundPermMig = rlsFindings.stream().anyMatch(f -> f.filePath().contains("20260831_permissions_database_sync.sql"));
        assertTrue(foundSurveyMig, "Must flag multi-line RLS bypass in 20260917_surveys_and_responses.sql");
        assertTrue(foundPermMig, "Must flag multi-line RLS bypass in 20260831_permissions_database_sync.sql");
    }

    @Test
    void testFunctionScopedPrngPinDetected(@TempDir Path tempDir) throws IOException {
        Path storeDir = tempDir.resolve("src/store");
        Files.createDirectories(storeDir);
        Path storeFile = storeDir.resolve("smartLotStore.ts");
        Files.writeString(storeFile, """
                function generateSecurePin(min = 1000, max = 9999): string {
                  if (typeof window !== 'undefined' && window.crypto && window.crypto.getRandomValues) {
                    const arr = new Uint32Array(1);
                    window.crypto.getRandomValues(arr);
                    const range = max - min + 1;
                    return (min + (arr[0] % range)).toString();
                  }
                  return Math.floor(min + Math.random() * (max - min + 1)).toString();
                }
                
                function generateSecureToken(prefix = 'INV'): string {
                  if (typeof window !== 'undefined' && window.crypto && window.crypto.randomUUID) {
                    return `${prefix}-${window.crypto.randomUUID().replace(/-/g, '').substring(0, 10).toUpperCase()}`;
                  }
                  return `${prefix}-${Date.now().toString(36).toUpperCase()}`;
                }
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        boolean foundPin = findings.stream().anyMatch(f -> "CR-RAND-001".equals(f.ruleId()));
        boolean foundToken = findings.stream().anyMatch(f -> "CR-RAND-002".equals(f.ruleId()));
        assertTrue(foundPin, "Should detect CR-RAND-001 inside generateSecurePin helper");
        assertTrue(foundToken, "Should detect CR-RAND-002 inside generateSecureToken helper");
    }

    @Test
    void testParameterSecurityRulesDetected(@TempDir Path tempDir) throws IOException {
        Path apiDir = tempDir.resolve("src/routes");
        Files.createDirectories(apiDir);
        Path routeFile = apiDir.resolve("userRoutes.ts");
        Files.writeString(routeFile, """
                import { Router } from 'express';
                const router = Router();

                // CR-PARAM-001: Mass assignment
                router.post('/register', async (req, res) => {
                    const user = await prisma.user.create(req.body);
                    res.json(user);
                });

                // CR-PARAM-002: IDOR
                router.delete('/documents/:id', async (req, res) => {
                    await db.document.delete(req.params.id);
                    res.sendStatus(204);
                });

                // CR-PARAM-003: Open redirect
                router.get('/login-redirect', (req, res) => {
                    res.redirect(req.query.returnUrl);
                });

                // CR-PARAM-004: Unbounded pagination
                router.get('/items', async (req, res) => {
                    const limit = parseInt(req.query.limit);
                    const items = await db.items.findMany({ take: limit });
                    res.json(items);
                });

                // CR-PARAM-005: Prototype pollution
                router.patch('/profile', (req, res) => {
                    const target = {};
                    Object.assign(target, req.body);
                    res.json(target);
                });
                """);

        RuleContext ctx = new RuleContext(null, new PipelineContext(UUID.randomUUID(), tempDir));
        List<RuleFinding> findings = rule.evaluate(ctx);

        assertTrue(findings.stream().anyMatch(f -> "CR-PARAM-001".equals(f.ruleId())), "Must detect CR-PARAM-001 Mass Assignment");
        assertTrue(findings.stream().anyMatch(f -> "CR-PARAM-002".equals(f.ruleId())), "Must detect CR-PARAM-002 IDOR on Resource Parameter");
        assertTrue(findings.stream().anyMatch(f -> "CR-PARAM-003".equals(f.ruleId())), "Must detect CR-PARAM-003 Open Redirect");
        assertTrue(findings.stream().anyMatch(f -> "CR-PARAM-004".equals(f.ruleId())), "Must detect CR-PARAM-004 Unbounded Pagination");
        assertTrue(findings.stream().anyMatch(f -> "CR-PARAM-005".equals(f.ruleId())), "Must detect CR-PARAM-005 Prototype Pollution");
    }
}
