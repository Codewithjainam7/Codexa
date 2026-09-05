import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import Header from './components/Header';
import Footer from './components/Footer';
import LandingView from './components/LandingView';
import NewAnalysisView from './components/NewAnalysisView';
import AnalysisDetailView from './components/AnalysisDetailView';
import { checkHealth, getLimits } from './api/client';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import FlickeringGrid from './components/ui/FlickeringGrid';
import BackgroundRippleEffect from './components/ui/BackgroundRippleEffect';

function MainApp() {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
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

  const pageTransition = {
    initial: { opacity: 0, y: 16 },
    animate: { opacity: 1, y: 0 },
    exit: { opacity: 0, y: -16 },
    transition: { duration: 0.3, ease: [0.16, 1, 0.3, 1] }
  };

  return (
    <div className="min-h-screen flex flex-col text-[var(--text-primary)] selection:bg-blue-500/20 selection:text-blue-600 dark:selection:text-blue-300 transition-colors duration-250 relative overflow-x-hidden">
      {/* 1. Global Viewport Full-Screen Base Background & Ripple Effect (z-0, 100% edge-to-edge) */}
      <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
        {/* Solid Base Background Surface */}
        <div className="absolute inset-0 bg-[var(--bg-base)]" />

        {/* Flickering Grid Pattern */}
        <FlickeringGrid
          className="w-full h-full opacity-80"
          squareSize={4}
          gridGap={6}
          color={isDark ? "#3B82F6" : "#2563EB"}
          maxOpacity={isDark ? 0.28 : 0.20}
          flickerChance={0.14}
        />

        {/* Dynamic Water Ripple Effect */}
        <BackgroundRippleEffect interactive={true} />

        {/* Soft Radial Ambient Vignette */}
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(37,99,235,0.06),rgba(255,255,255,0))] dark:bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(59,130,246,0.12),rgba(0,0,0,0))] pointer-events-none" />

        {/* Ambient background light orbs (Desktop only for max mobile performance) */}
        <div className="hidden md:block absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[850px] h-[500px] bg-blue-600/12 dark:bg-blue-600/20 rounded-full blur-[140px] pointer-events-none" />
        <div className="hidden md:block absolute bottom-1/3 right-1/4 w-[600px] h-[400px] bg-indigo-600/10 dark:bg-indigo-600/15 rounded-full blur-[130px] pointer-events-none" />
        <div className="hidden md:block absolute top-2/3 left-1/4 w-[500px] h-[350px] bg-sky-500/8 dark:bg-sky-500/12 rounded-full blur-[130px] pointer-events-none" />
      </div>

      <Header
        currentView={currentView}
        setCurrentView={setCurrentView}
        isConnected={isConnected}
      />

      {/* Main Content Area - Strictly Confined Above Footer */}
      <div className="flex-1 relative flex flex-col pt-20 sm:pt-24">
        <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-0 relative z-10">
          <AnimatePresence mode="wait">
            {currentView === 'landing' && (
              <motion.div
                key="landing-view"
                initial={pageTransition.initial}
                animate={pageTransition.animate}
                exit={pageTransition.exit}
                transition={pageTransition.transition}
              >
                <LandingView onStartAnalysis={() => setCurrentView('upload')} />
              </motion.div>
            )}

            {currentView === 'upload' && (
              <motion.div
                key="upload-view"
                initial={pageTransition.initial}
                animate={pageTransition.animate}
                exit={pageTransition.exit}
                transition={pageTransition.transition}
              >
                <NewAnalysisView
                  limits={limits}
                  onJobCreated={handleJobCreated}
                />
              </motion.div>
            )}

            {currentView === 'analysis' && activeJobId && (
              <motion.div
                key={`analysis-view-${activeJobId}`}
                initial={pageTransition.initial}
                animate={pageTransition.animate}
                exit={pageTransition.exit}
                transition={pageTransition.transition}
              >
                <AnalysisDetailView
                  jobId={activeJobId}
                  onBack={() => setCurrentView('landing')}
                />
              </motion.div>
            )}
          </AnimatePresence>
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
