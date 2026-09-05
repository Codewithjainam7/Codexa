import { Bot, Atom, Terminal, Brain, Shield, Sparkles, Binary, Rocket } from 'lucide-react';

export const TECH_AVATARS = [
  { id: 'sentinel', name: 'Cyber Sentinel', role: 'Security Bot', icon: Bot, color: 'from-blue-500 to-cyan-500', text: 'text-cyan-400', bg: 'bg-cyan-500/15', border: 'border-cyan-500/30' },
  { id: 'quantum', name: 'Quantum Core', role: 'Kernel Dev', icon: Atom, color: 'from-indigo-500 to-violet-500', text: 'text-violet-400', bg: 'bg-violet-500/15', border: 'border-violet-500/30' },
  { id: 'hacker', name: 'Terminal Hacker', role: 'Pen Tester', icon: Terminal, color: 'from-emerald-500 to-teal-500', text: 'text-emerald-400', bg: 'bg-emerald-500/15', border: 'border-emerald-500/30' },
  { id: 'neural', name: 'Neural Synapse', role: 'AI Architect', icon: Brain, color: 'from-pink-500 to-rose-500', text: 'text-pink-400', bg: 'bg-pink-500/15', border: 'border-pink-500/30' },
  { id: 'shield', name: 'Zero Trust', role: 'SOC Defender', icon: Shield, color: 'from-amber-500 to-orange-500', text: 'text-amber-400', bg: 'bg-amber-500/15', border: 'border-amber-500/30' },
  { id: 'daemon', name: 'Glitch Daemon', role: 'Bug Hunter', icon: Sparkles, color: 'from-purple-500 to-indigo-600', text: 'text-purple-400', bg: 'bg-purple-500/15', border: 'border-purple-500/30' },
  { id: 'silicon', name: 'Bio Silicon', role: 'AST Parser', icon: Binary, color: 'from-teal-500 to-emerald-600', text: 'text-teal-400', bg: 'bg-teal-500/15', border: 'border-teal-500/30' },
  { id: 'warp', name: 'Hyper Warp', role: 'Cloud Lead', icon: Rocket, color: 'from-sky-500 to-blue-600', text: 'text-sky-400', bg: 'bg-sky-500/15', border: 'border-sky-500/30' },
];

export function getActiveAvatar() {
  if (typeof window === 'undefined') return TECH_AVATARS[0];
  const saved = localStorage.getItem('codexa_user_avatar');
  return TECH_AVATARS.find(a => a.id === saved) || TECH_AVATARS[0];
}

export function setActiveAvatar(id) {
  if (typeof window === 'undefined') return;
  localStorage.setItem('codexa_user_avatar', id);
  window.dispatchEvent(new Event('codexa_avatar_updated'));
}
