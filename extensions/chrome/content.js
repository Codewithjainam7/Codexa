// Codexa Chrome Extension - Content Script for GitHub PR & Repository Integration
(function() {
  'use strict';

  const CODEXA_ENDPOINT = 'https://codexa-ye85.onrender.com';

  function injectCodexaBadge() {
    if (document.getElementById('codexa-github-badge')) return;

    // Detect GitHub Pull Request or Repository header
    const prHeader = document.querySelector('.gh-header-actions') || 
                     document.querySelector('.pagehead-actions') ||
                     document.querySelector('#partial-discussion-header');

    if (!prHeader) return;

    const currentUrl = window.location.href;
    const badgeContainer = document.createElement('div');
    badgeContainer.id = 'codexa-github-badge';
    badgeContainer.style.cssText = `
      display: inline-flex;
      align-items: center;
      gap: 6px;
      margin-left: 8px;
      vertical-align: middle;
    `;

    const auditBtn = document.createElement('button');
    auditBtn.type = 'button';
    auditBtn.innerHTML = `
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 4px; vertical-align: -2px;">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
      </svg>
      <span>Codexa Audit</span>
    `;
    auditBtn.style.cssText = `
      background: #0f172a;
      color: #38bdf8;
      border: 1px solid #0284c7;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 600;
      padding: 3px 10px;
      cursor: pointer;
      transition: all 0.2s ease;
      display: inline-flex;
      align-items: center;
    `;

    auditBtn.addEventListener('mouseenter', () => {
      auditBtn.style.background = '#0284c7';
      auditBtn.style.color = '#ffffff';
    });
    auditBtn.addEventListener('mouseleave', () => {
      auditBtn.style.background = '#0f172a';
      auditBtn.style.color = '#38bdf8';
    });

    auditBtn.addEventListener('click', () => {
      const match = window.location.pathname.match(/^\/([^\/]+)\/([^\/]+)/);
      if (match) {
        const repoUrl = `https://github.com/${match[1]}/${match[2]}`;
        window.open(`${CODEXA_ENDPOINT}/?repo=${encodeURIComponent(repoUrl)}`, '_blank');
      } else {
        window.open(CODEXA_ENDPOINT, '_blank');
      }
    });

    badgeContainer.appendChild(auditBtn);
    prHeader.appendChild(badgeContainer);
  }

  // Observe SPA page navigation on GitHub
  const observer = new MutationObserver(() => {
    injectCodexaBadge();
  });

  observer.observe(document.body, { childList: true, subtree: true });
  injectCodexaBadge();
})();
