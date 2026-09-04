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
    <div className="relative inline-block text-left" ref={dropdownRef}>
      <div className="flex items-center space-x-2">
        {Icon && <Icon className="w-3.5 h-3.5 text-slate-400 shrink-0" />}
        {label && <span className="text-xs text-slate-400 font-medium shrink-0">{label}:</span>}
        
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          className={`flex items-center justify-between space-x-2 px-3 py-1.5 rounded-xl text-xs font-semibold border transition-all shadow-sm ${
            isOpen 
              ? 'bg-slate-900 border-emerald-500/60 text-white ring-1 ring-emerald-500/30' 
              : 'bg-slate-950/80 hover:bg-slate-900 border-slate-800 text-slate-200 hover:border-slate-700'
          }`}
        >
          <span className="truncate">{selectedOption ? selectedOption.label : 'Select...'}</span>
          <ChevronDown className={`w-3.5 h-3.5 text-slate-400 transition-transform duration-200 ${isOpen ? 'rotate-180 text-emerald-400' : ''}`} />
        </button>
      </div>

      {/* Dropdown Menu Modal */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-48 rounded-2xl bg-slate-900 border border-slate-800/90 shadow-2xl z-50 p-1.5 space-y-0.5 backdrop-blur-xl animate-in fade-in zoom-in-95 duration-100">
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
                className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-medium transition-all ${
                  isSelected 
                    ? 'bg-emerald-500/15 text-emerald-300 font-semibold border border-emerald-500/30' 
                    : 'text-slate-300 hover:bg-slate-800/80 hover:text-white'
                }`}
              >
                <div className="flex items-center space-x-2">
                  {opt.dotColor && (
                    <span className={`w-2 h-2 rounded-full ${opt.dotColor}`} />
                  )}
                  <span>{opt.label}</span>
                </div>
                {isSelected && <Check className="w-3.5 h-3.5 text-emerald-400" />}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
