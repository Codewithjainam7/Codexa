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
    <div className="relative inline-block text-left z-40" ref={dropdownRef}>
      <div className="flex items-center space-x-2">
        {Icon && <Icon className="w-3.5 h-3.5 text-[var(--text-muted)] shrink-0" />}
        {label && <span className="text-xs text-[var(--text-muted)] font-medium shrink-0">{label}:</span>}
        
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className={`flex items-center justify-between space-x-2 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all shadow-sm cursor-pointer ${
            isOpen 
              ? 'bg-[var(--bg-card)] border-blue-500/60 text-[var(--text-primary)] ring-1 ring-blue-500/30' 
              : 'bg-[var(--bg-recessed)] hover:bg-[var(--bg-card)] border-[var(--border-subtle)] text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
          }`}
        >
          <span className="truncate">{selectedOption ? selectedOption.label : 'Select...'}</span>
          <ChevronDown className={`w-3.5 h-3.5 transition-transform duration-200 ${isOpen ? 'rotate-180 text-blue-500' : 'text-[var(--text-muted)]'}`} />
        </button>
      </div>

      {/* Dropdown Menu Modal - Highest Z-Index Priority */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-48 rounded-2xl bg-[var(--bg-card)] border border-[var(--border-subtle)] shadow-2xl z-[100] p-1.5 space-y-0.5 backdrop-blur-2xl animate-in fade-in zoom-in-95 duration-100">
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
                    ? 'bg-blue-500/15 text-blue-600 dark:text-blue-400 font-semibold border border-blue-500/30' 
                    : 'text-[var(--text-secondary)] hover:bg-[var(--bg-recessed)] hover:text-[var(--text-primary)]'
                }`}
              >
                <div className="flex items-center space-x-2">
                  {opt.dotColor && (
                    <span className={`w-2 h-2 rounded-full ${opt.dotColor}`} />
                  )}
                  <span>{opt.label}</span>
                </div>
                {isSelected && <Check className="w-3.5 h-3.5 text-blue-500" />}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
