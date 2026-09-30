# Codexa Chrome & Chromium Extension Guide

## 1. Overview
The Codexa Chrome Extension integrates automated AST code review directly into GitHub pull requests and repository pages. It eliminates context switching by allowing developers to trigger code audits directly from their browser while reviewing code.

---

## 2. Directory Structure
All extension source code is maintained in [`extensions/chrome/`](file:///F:/Codexa/extensions/chrome/):
```
extensions/chrome/
├── manifest.json      # Manifest V3 extension configuration
├── content.js         # Content script injected into GitHub PR and repo pages
├── popup.html         # Quick-action popup UI
├── popup.js           # Extension popup event logic
└── README.md          # Setup and developer instructions
```

---

## 3. Features
1. **GitHub PR Action Bar Button**: Automatically injects a stylish, branded **"Codexa Audit"** button next to GitHub's native PR merge controls.
2. **One-Click Audit**: Clicking the button reads the active repository URL and routes directly to Codexa with the URL prefilled.
3. **Engine Status Monitor**: The popup displays live backend health and provides quick links to the Security Playground and main dashboard.

---

## 4. Installation Instructions (Developer Mode)
1. In Google Chrome, Brave, or Microsoft Edge, navigate to `chrome://extensions`.
2. Toggle the **Developer mode** switch in the top-right corner to **On**.
3. Click the **Load unpacked** button in the top-left toolbar.
4. Select the local directory: `F:\Codexa\extensions\chrome`.
5. The extension is installed and ready to use!
