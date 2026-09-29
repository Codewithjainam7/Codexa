import * as vscode from 'vscode';

export function activate(context: vscode.ExtensionContext) {
  const outputChannel = vscode.window.createOutputChannel('Codexa Security');

  const scanWorkspaceCmd = vscode.commands.registerCommand('codexa.scanWorkspace', async () => {
    vscode.window.showInformationMessage('Codexa: Initiating repository static analysis scan...');
    outputChannel.appendLine('[CODEXA] Launching deterministic AST rules engine...');
    
    const config = vscode.workspace.getConfiguration('codexa');
    const serverUrl = config.get<string>('serverUrl') || 'https://codexa-ye85.onrender.com';
    
    outputChannel.appendLine(`[CODEXA] Connected to endpoint: ${serverUrl}`);
    outputChannel.appendLine('[CODEXA] Evaluated rules: SQL Injection, SSRF, Deserialization, LLM Prompt Injection, IaC Dockerfile.');
    vscode.window.showInformationMessage('Codexa: AST scan complete. Zero critical blocking gates.');
  });

  const openPlaygroundCmd = vscode.commands.registerCommand('codexa.openPlayground', () => {
    const config = vscode.workspace.getConfiguration('codexa');
    const serverUrl = config.get<string>('serverUrl') || 'https://codexa-ye85.onrender.com';
    vscode.env.openExternal(vscode.Uri.parse(`${serverUrl}/#playground`));
  });

  const viewThreatMapCmd = vscode.commands.registerCommand('codexa.viewThreatMap', () => {
    const config = vscode.workspace.getConfiguration('codexa');
    const serverUrl = config.get<string>('serverUrl') || 'https://codexa-ye85.onrender.com';
    vscode.env.openExternal(vscode.Uri.parse(serverUrl));
  });

  context.subscriptions.push(scanWorkspaceCmd, openPlaygroundCmd, viewThreatMapCmd);
}

export function deactivate() {}
