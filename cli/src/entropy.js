/**
 * Shannon Entropy calculation module for CLI secret scanning.
 */

function calculateShannonEntropy(str) {
  if (!str || str.length === 0) return 0;
  const freqs = {};
  for (let i = 0; i < str.length; i++) {
    const c = str[i];
    freqs[c] = (freqs[c] || 0) + 1;
  }
  let entropy = 0;
  const len = str.length;
  for (const count of Object.values(freqs)) {
    const p = count / len;
    entropy -= p * Math.log2(p);
  }
  return Math.round(entropy * 1000) / 1000;
}

function isHighEntropyToken(token) {
  if (!token || token.length < 16) return false;
  const entropy = calculateShannonEntropy(token);
  if (/^[0-9a-fA-F]+$/.test(token)) {
    return entropy >= 3.2 && token.length >= 24;
  }
  return entropy >= 4.5 && token.length >= 20;
}

module.exports = {
  calculateShannonEntropy,
  isHighEntropyToken
};
