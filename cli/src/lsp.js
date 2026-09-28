/**
 * Codexa Language Server Protocol (LSP) Daemon
 * Implements JSON-RPC 2.0 over stdio for IDE integration (VS Code, Cursor, Neovim).
 */

const { scanFile } = require("./scanner");

function startLspServer() {
  let buffer = Buffer.alloc(0);

  function sendResponse(id, result) {
    const payload = JSON.stringify({ jsonrpc: "2.0", id, result });
    const message = `Content-Length: ${Buffer.byteLength(payload, "utf8")}\r\n\r\n${payload}`;
    process.stdout.write(message);
  }

  function sendNotification(method, params) {
    const payload = JSON.stringify({ jsonrpc: "2.0", method, params });
    const message = `Content-Length: ${Buffer.byteLength(payload, "utf8")}\r\n\r\n${payload}`;
    process.stdout.write(message);
  }

  function publishDiagnosticsForFile(fileUri, filePath) {
    try {
      const { findings } = scanFile(filePath, process.cwd());
      const diagnostics = findings.map(f => {
        let severity = 1; // Error
        if (f.severity === "MEDIUM") severity = 2; // Warning
        else if (f.severity === "LOW" || f.severity === "INFO") severity = 3; // Information

        return {
          range: {
            start: { line: Math.max(0, f.line - 1), character: 0 },
            end: { line: Math.max(0, f.line - 1), character: 120 }
          },
          severity,
          code: f.ruleId,
          source: "Codexa SAST",
          message: `[${f.severity}] ${f.title}\n\n${f.description}\n\nRemediation:\n${f.remediation}`
        };
      });

      sendNotification("textDocument/publishDiagnostics", {
        uri: fileUri,
        diagnostics
      });
    } catch (e) {
      // Ignore file reading errors during live editing
    }
  }

  function handleMessage(msg) {
    if (msg.method === "initialize") {
      sendResponse(msg.id, {
        capabilities: {
          textDocumentSync: 1, // Full sync
          diagnosticProvider: {
            interFileDependencies: false,
            workspaceDiagnostics: false
          }
        },
        serverInfo: {
          name: "Codexa Language Server",
          version: "1.0.0"
        }
      });
    } else if (msg.method === "textDocument/didOpen") {
      const uri = msg.params.textDocument.uri;
      const filePath = uri.replace(/^file:\/\/\/?/, "");
      publishDiagnosticsForFile(uri, filePath);
    } else if (msg.method === "textDocument/didSave") {
      const uri = msg.params.textDocument.uri;
      const filePath = uri.replace(/^file:\/\/\/?/, "");
      publishDiagnosticsForFile(uri, filePath);
    } else if (msg.method === "shutdown") {
      sendResponse(msg.id, null);
    } else if (msg.method === "exit") {
      process.exit(0);
    }
  }

  process.stdin.on("data", (chunk) => {
    buffer = Buffer.concat([buffer, chunk]);
    while (true) {
      const headerEnd = buffer.indexOf("\r\n\r\n");
      if (headerEnd === -1) break;

      const headerText = buffer.slice(0, headerEnd).toString("utf8");
      const match = headerText.match(/Content-Length:\s*(\d+)/i);
      if (!match) {
        buffer = Buffer.alloc(0);
        break;
      }

      const contentLength = parseInt(match[1], 10);
      const totalLength = headerEnd + 4 + contentLength;
      if (buffer.length < totalLength) break;

      const body = buffer.slice(headerEnd + 4, totalLength).toString("utf8");
      buffer = buffer.slice(totalLength);

      try {
        const msg = JSON.parse(body);
        handleMessage(msg);
      } catch (err) {
        // malformed json
      }
    }
  });

  process.stderr.write("Codexa LSP Server initialized and listening on stdio.\n");
}

module.exports = {
  startLspServer
};
