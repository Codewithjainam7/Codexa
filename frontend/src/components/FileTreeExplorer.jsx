import React, { useState, useMemo } from 'react';
import { 
  Folder, FolderOpen, FileCode, AlertCircle, ShieldAlert, 
  ChevronRight, ChevronDown, CheckCircle, File, Search
} from 'lucide-react';

export default function FileTreeExplorer({ findings = [], selectedFile, onSelectFile }) {
  const [expandedFolders, setExpandedFolders] = useState({});
  const [fileSearch, setFileSearch] = useState('');

  // 1. Compute severity per file
  const fileSeverityMap = useMemo(() => {
    const map = {};
    findings.forEach(f => {
      if (!f.filePath) return;
      // Normalize file path: strip staging prefixes if present
      const cleanPath = f.filePath.replace(/^[^\/]+\//, '');
      const rawPath = f.filePath;

      [cleanPath, rawPath].forEach(p => {
        if (!map[p]) {
          map[p] = {
            critical: 0,
            high: 0,
            medium: 0,
            low: 0,
            info: 0,
            total: 0,
            highestSeverity: 'CLEAN'
          };
        }
        const sev = (f.severity || 'LOW').toUpperCase();
        map[p].total += 1;
        if (sev === 'CRITICAL') {
          map[p].critical += 1;
          map[p].highestSeverity = 'CRITICAL';
        } else if (sev === 'HIGH') {
          map[p].high += 1;
          if (map[p].highestSeverity !== 'CRITICAL') map[p].highestSeverity = 'HIGH';
        } else if (sev === 'MEDIUM') {
          map[p].medium += 1;
          if (!['CRITICAL', 'HIGH'].includes(map[p].highestSeverity)) map[p].highestSeverity = 'MEDIUM';
        } else {
          map[p].low += 1;
          if (!['CRITICAL', 'HIGH', 'MEDIUM'].includes(map[p].highestSeverity)) map[p].highestSeverity = 'LOW';
        }
      });
    });
    return map;
  }, [findings]);

  // 2. Build Tree Structure
  const treeData = useMemo(() => {
    const root = { name: 'root', type: 'dir', children: {}, path: '' };

    const filePaths = Array.from(new Set(findings.map(f => f.filePath.replace(/^[^\/]+\//, '')).filter(Boolean)));

    filePaths.forEach(filePath => {
      const parts = filePath.split('/');
      let current = root;

      parts.forEach((part, index) => {
        const isFile = index === parts.length - 1;
        const currentPath = parts.slice(0, index + 1).join('/');

        if (!current.children[part]) {
          current.children[part] = {
            name: part,
            type: isFile ? 'file' : 'dir',
            path: currentPath,
            children: isFile ? null : {}
          };
        }
        current = current.children[part];
      });
    });

    return root;
  }, [findings]);

  const toggleFolder = (path) => {
    setExpandedFolders(prev => ({
      ...prev,
      [path]: prev[path] === undefined ? false : !prev[path]
    }));
  };

  const getSeverityStyle = (highestSev) => {
    switch (highestSev) {
      case 'CRITICAL':
        return {
          textColor: 'text-rose-400 font-semibold',
          badgeBg: 'bg-rose-500/20 text-rose-300 border-rose-500/40',
          dot: 'bg-rose-500 animate-pulse'
        };
      case 'HIGH':
        return {
          textColor: 'text-orange-400 font-semibold',
          badgeBg: 'bg-orange-500/20 text-orange-300 border-orange-500/40',
          dot: 'bg-orange-500'
        };
      case 'MEDIUM':
        return {
          textColor: 'text-amber-400',
          badgeBg: 'bg-amber-500/20 text-amber-300 border-amber-500/40',
          dot: 'bg-amber-400'
        };
      case 'LOW':
        return {
          textColor: 'text-blue-400',
          badgeBg: 'bg-blue-500/20 text-blue-300 border-blue-500/40',
          dot: 'bg-blue-400'
        };
      default:
        return {
          textColor: 'text-slate-400',
          badgeBg: 'bg-slate-800 text-slate-400 border-slate-700',
          dot: 'bg-slate-600'
        };
    }
  };

  const renderNode = (node, depth = 0) => {
    if (!node || node.name === 'root') {
      return Object.values(node?.children || {}).map(child => renderNode(child, depth));
    }

    const isFolder = node.type === 'dir';
    const isExpanded = expandedFolders[node.path] !== false; // default open
    const fileStats = fileSeverityMap[node.path];
    const isSelected = selectedFile === node.path || (selectedFile && selectedFile.endsWith(node.path));

    if (fileSearch && !node.path.toLowerCase().includes(fileSearch.toLowerCase())) {
      return null;
    }

    const sevStyle = fileStats ? getSeverityStyle(fileStats.highestSeverity) : getSeverityStyle('CLEAN');

    return (
      <div key={node.path} className="select-none text-xs">
        <div
          onClick={() => {
            if (isFolder) {
              toggleFolder(node.path);
            } else {
              onSelectFile(node.path === selectedFile ? '' : node.path);
            }
          }}
          style={{ paddingLeft: `${depth * 14 + 8}px` }}
          className={`flex items-center justify-between py-1.5 pr-2 rounded-lg cursor-pointer transition-all duration-150 ${
            isSelected 
              ? 'bg-emerald-500/20 border border-emerald-500/40 text-emerald-300' 
              : 'hover:bg-slate-800/60 text-slate-300'
          }`}
        >
          <div className="flex items-center space-x-1.5 min-w-0 pr-2">
            {isFolder ? (
              <>
                <span className="text-slate-500 w-3.5 h-3.5 flex items-center justify-center">
                  {isExpanded ? <ChevronDown className="w-3.5 h-3.5" /> : <ChevronRight className="w-3.5 h-3.5" />}
                </span>
                {isExpanded ? (
                  <FolderOpen className="w-4 h-4 text-amber-400 shrink-0" />
                ) : (
                  <Folder className="w-4 h-4 text-amber-400/80 shrink-0" />
                )}
                <span className="truncate font-medium text-slate-200">{node.name}</span>
              </>
            ) : (
              <>
                <span className="w-3.5 h-3.5 flex items-center justify-center shrink-0">
                  <span className={`w-2 h-2 rounded-full ${sevStyle.dot}`} />
                </span>
                <FileCode className={`w-4 h-4 shrink-0 ${fileStats ? sevStyle.textColor : 'text-slate-400'}`} />
                <span className={`truncate ${fileStats ? sevStyle.textColor : 'text-slate-300'}`}>
                  {node.name}
                </span>
              </>
            )}
          </div>

          {/* Finding Count Badge */}
          {fileStats && fileStats.total > 0 && (
            <span className={`px-1.5 py-0.2 rounded border text-[10px] font-mono shrink-0 font-bold ${sevStyle.badgeBg}`}>
              {fileStats.total}
            </span>
          )}
        </div>

        {isFolder && isExpanded && node.children && (
          <div className="space-y-0.5">
            {Object.values(node.children).map(child => renderNode(child, depth + 1))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="bg-slate-900/70 border border-slate-800 rounded-2xl p-4 space-y-3 flex flex-col h-full">
      <div className="flex items-center justify-between pb-2 border-b border-slate-800">
        <div className="flex items-center space-x-2">
          <Folder className="w-4 h-4 text-emerald-400" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-white">Repository Files</h3>
        </div>
        {selectedFile && (
          <button
            onClick={() => onSelectFile('')}
            className="text-[10px] text-emerald-400 hover:text-emerald-300 underline font-medium"
          >
            Clear Filter
          </button>
        )}
      </div>

      {/* Quick Search */}
      <div className="relative">
        <Search className="w-3.5 h-3.5 text-slate-500 absolute left-2.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={fileSearch}
          onChange={(e) => setFileSearch(e.target.value)}
          placeholder="Filter files..."
          className="w-full pl-8 pr-2.5 py-1 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
        />
      </div>

      {/* File Tree List */}
      <div className="overflow-y-auto max-h-[500px] space-y-0.5 pr-1 custom-scrollbar">
        {findings.length === 0 ? (
          <div className="text-center py-6 text-xs text-slate-500">No source files with issues</div>
        ) : (
          renderNode(treeData)
        )}
      </div>

      {/* Severity Legend */}
      <div className="pt-2 border-t border-slate-800/80 flex items-center justify-between text-[10px] text-slate-400 font-mono">
        <span className="flex items-center space-x-1">
          <span className="w-2 h-2 rounded-full bg-rose-500" />
          <span>Critical</span>
        </span>
        <span className="flex items-center space-x-1">
          <span className="w-2 h-2 rounded-full bg-orange-500" />
          <span>High</span>
        </span>
        <span className="flex items-center space-x-1">
          <span className="w-2 h-2 rounded-full bg-amber-400" />
          <span>Medium</span>
        </span>
        <span className="flex items-center space-x-1">
          <span className="w-2 h-2 rounded-full bg-blue-400" />
          <span>Low</span>
        </span>
      </div>
    </div>
  );
}
