import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import Footer from './components/Footer';
import LandingView from './components/LandingView';
import NewAnalysisView from './components/NewAnalysisView';
import AnalysisDetailView from './components/AnalysisDetailView';
import { checkHealth, getLimits } from './api/client';
import { ThemeProvider } from './context/ThemeContext';

import BackgroundRippleEffect from './components/ui/BackgroundRippleEffect';

function MainApp() {
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
    <div className="min-h-screen flex flex-col bg-[var(--bg-base)] text-[var(--text-primary)] selection:bg-amber-500/20 selection:text-amber-600 dark:selection:text-amber-200 transition-colors duration-250 relative overflow-x-hidden">
      {/* Background Ripple Waves Across Entire Site (Behind All Views, Excluded From Footer) */}
      <BackgroundRippleEffect
        numCircles={9}
        mainCircleSize={260}
        mainCircleOpacity={0.28}
        interactive={true}
        focalPoints={[
          { x: "50%", y: "220px", size: 320, scale: 1.2 },
          { x: "88%", y: "450px", size: 240, scale: 0.9 },
          { x: "10%", y: "780px", size: 260, scale: 1.0 },
          { x: "75%", y: "1350px", size: 280, scale: 1.1 },
          { x: "20%", y: "1950px", size: 260, scale: 0.95 }
        ]}
      />

      <Header
        currentView={currentView}
        setCurrentView={setCurrentView}
        isConnected={isConnected}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-0 relative z-10">
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

export default function App() {
  return (
    <ThemeProvider>
      <MainApp />
    </ThemeProvider>
  );
}
