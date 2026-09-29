document.addEventListener('DOMContentLoaded', () => {
  const CODEXA_URL = 'https://codexa-ye85.onrender.com';

  document.getElementById('btn-scan').addEventListener('click', async () => {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (tab && tab.url && tab.url.includes('github.com')) {
      const match = tab.url.match(/^https:\/\/github\.com\/([^\/]+)\/([^\/]+)/);
      if (match) {
        const repo = `https://github.com/${match[1]}/${match[2]}`;
        chrome.tabs.create({ url: `${CODEXA_URL}/?repo=${encodeURIComponent(repo)}` });
        return;
      }
    }
    chrome.tabs.create({ url: `${CODEXA_URL}` });
  });

  document.getElementById('btn-playground').addEventListener('click', () => {
    chrome.tabs.create({ url: `${CODEXA_URL}/#playground` });
  });

  document.getElementById('btn-dashboard').addEventListener('click', () => {
    chrome.tabs.create({ url: CODEXA_URL });
  });
});
