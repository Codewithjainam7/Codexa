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
    <div className="min-h-screen flex flex-col bg-[var(--bg-base)] text-[var(--text-primary)] selection:bg-blue-500/20 selection:text-blue-600 dark:selection:text-blue-300 transition-colors duration-250 relative overflow-x-hidden">
      <Header
        currentView={currentView}
        setCurrentView={setCurrentView}
        isConnected={isConnected}
      />

      {/* Main Content Area with Background Ripple Effects - Strictly Confined Above Footer */}
      <div className="flex-1 relative overflow-hidden flex flex-col">
        <BackgroundRippleEffect
          numCircles={8}
          mainCircleSize={240}
          mainCircleOpacity={0.12}
          interactive={true}
          focalPoints={[
            { x: "50%", y: "220px", size: 300, scale: 1.15 },
            { x: "85%", y: "450px", size: 220, scale: 0.9 },
            { x: "12%", y: "750px", size: 240, scale: 1.0 }
          ]}
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
      </div>

      {/* Footer has completely solid, opaque background with high z-index - NO ripples here */}
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
