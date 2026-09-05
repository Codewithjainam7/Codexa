import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Check } from 'lucide-react';

export default function CustomSelect({ label, value, onChange, options = [], icon: Icon }) {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  const selectedOption = options.find(o => o.value === value) || options[0];

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className={`relative inline-block text-left ${isOpen ? 'z-50' : 'z-30'}`} ref={dropdownRef}>
      <div className="flex items-center space-x-1.5 sm:space-x-2">
        {Icon && <Icon className="w-3.5 h-3.5 text-[var(--text-muted)] shrink-0" />}
        {label && <span className="text-[11px] sm:text-xs text-[var(--text-muted)] font-medium shrink-0">{label}:</span>}
        
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className={`flex items-center justify-between space-x-1.5 sm:space-x-2 px-2.5 sm:px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all shadow-sm cursor-pointer min-w-[110px] sm:min-w-[130px] ${
            isOpen 
              ? 'bg-[var(--bg-card)] border-blue-500 text-[var(--text-primary)] ring-2 ring-blue-500/20' 
              : 'bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border-[var(--border-subtle)] text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
          }`}
        >
          <span className="truncate">{selectedOption ? selectedOption.label : 'Select...'}</span>
          <ChevronDown className={`w-3.5 h-3.5 transition-transform duration-200 shrink-0 ${isOpen ? 'rotate-180 text-blue-500' : 'text-[var(--text-muted)]'}`} />
        </button>
      </div>

      {/* Dropdown Menu Modal - 100% Solid Opaque Background (Zero Transparency Bleed) */}
      {isOpen && (
        <div className="absolute right-0 mt-1.5 w-52 rounded-2xl bg-white dark:bg-[#0B0F19] border border-slate-200 dark:border-slate-800 shadow-[0_16px_40px_rgba(0,0,0,0.4)] z-[100] p-1.5 space-y-0.5 animate-in fade-in zoom-in-95 duration-100">
          {options.map((opt) => {
            const isSelected = opt.value === value;
            return (
              <button
                key={opt.value}
                type="button"
                onClick={() => {
                  onChange(opt.value);
                  setIsOpen(false);
                }}
                className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-medium transition-all cursor-pointer ${
                  isSelected 
                    ? 'bg-blue-500/15 text-blue-600 dark:text-blue-400 font-bold border border-blue-500/30' 
                    : 'text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/80 hover:text-slate-900 dark:hover:text-white'
                }`}
              >
                <div className="flex items-center space-x-2 truncate">
                  {opt.dotColor && (
                    <span className={`w-2 h-2 rounded-full shrink-0 ${opt.dotColor}`} />
                  )}
                  <span className="truncate">{opt.label}</span>
                </div>
                {isSelected && <Check className="w-3.5 h-3.5 text-blue-500 shrink-0 ml-2" />}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
