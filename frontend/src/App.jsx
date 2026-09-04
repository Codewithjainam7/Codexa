import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import Footer from './components/Footer';
import LandingView from './components/LandingView';
import NewAnalysisView from './components/NewAnalysisView';
import AnalysisDetailView from './components/AnalysisDetailView';
import { checkHealth, getLimits } from './api/client';

export default function App() {
  const [currentView, setCurrentView] = useState('landing');
  const [activeJobId, setActiveJobId] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const [limits, setLimits] = useState(null);

  useEffect(() => {
    const initApp = async () => {
      try {
        await checkHealth();
        setIsConnected(true);
        const lData = await getLimits();
        setLimits(lData);
      } catch (e) {
        setIsConnected(false);
      }
    };

    initApp();
    const interval = setInterval(initApp, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleJobCreated = (jobResponse) => {
    setActiveJobId(jobResponse.id);
    setCurrentView('analysis');
  };

  return (
    <div className="min-h-screen flex flex-col bg-black text-neutral-100 selection:bg-white/20 selection:text-white">
      <Header
        currentView={currentView}
        setCurrentView={setCurrentView}
        isConnected={isConnected}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-0">
        {currentView === 'landing' && (
          <LandingView onStartAnalysis={() => setCurrentView('upload')} />
        )}

        {currentView === 'upload' && (
          <NewAnalysisView
            limits={limits}
            onJobCreated={handleJobCreated}
          />
        )}

        {currentView === 'analysis' && activeJobId && (
          <AnalysisDetailView
            jobId={activeJobId}
            onBack={() => setCurrentView('landing')}
          />
        )}
      </main>

      <Footer />
    </div>
  );
}
