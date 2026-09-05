import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import Header from './components/Header';
import Footer from './components/Footer';
import LandingView from './components/LandingView';
import MobileHomeView from './components/MobileHomeView';
import NewAnalysisView from './components/NewAnalysisView';
import AnalysisDetailView from './components/AnalysisDetailView';
import BottomNav from './components/BottomNav';
import MobileTopBar from './components/MobileTopBar';
import MobileSettingsView from './components/MobileSettingsView';
import SplashScreen from './components/SplashScreen';
import { checkHealth, getLimits } from './api/client';
import { ThemeProvider, useTheme } from './context/ThemeContext';
import FlickeringGrid from './components/ui/FlickeringGrid';
import BackgroundRippleEffect from './components/ui/BackgroundRippleEffect';

function MainApp() {
  const { theme } = useTheme();
  const isDark = theme === 'dark';
  const [isBooting, setIsBooting] = useState(true);
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
    initial: { opacity: 0, y: 10 },
    animate: { opacity: 1, y: 0 },
    exit: { opacity: 0, y: -10 },
    transition: { duration: 0.22, ease: [0.16, 1, 0.3, 1] }
  };

  return (
    <>
      {/* 0. Mobile Native Booting / Splash Animation */}
      <AnimatePresence>
        {isBooting && (
          <SplashScreen onFinish={() => setIsBooting(false)} />
        )}
      </AnimatePresence>

      <div className="min-h-screen flex flex-col text-[var(--text-primary)] selection:bg-blue-500/20 selection:text-blue-600 dark:selection:text-blue-300 transition-colors duration-250 relative overflow-x-hidden">
        {/* 1. Global Viewport Background (Clean on Mobile, Animated on Desktop) */}
        <div className="fixed inset-0 pointer-events-none z-0 overflow-hidden">
          {/* Solid Base Background Surface */}
          <div className="absolute inset-0 bg-[var(--bg-base)]" />

          {/* Flickering Grid Pattern (Desktop only for 60fps mobile speed) */}
          <FlickeringGrid
            className="w-full h-full opacity-80 hidden md:block"
            squareSize={4}
            gridGap={6}
            color={isDark ? "#3B82F6" : "#2563EB"}
            maxOpacity={isDark ? 0.28 : 0.20}
            flickerChance={0.14}
          />

          {/* Dynamic Water Ripple Effect (Desktop only) */}
          <BackgroundRippleEffect className="hidden md:block" interactive={true} />

          {/* Soft Radial Ambient Vignette (Desktop only) */}
          <div className="hidden md:block absolute inset-0 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(37,99,235,0.06),rgba(255,255,255,0))] dark:bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(59,130,246,0.12),rgba(0,0,0,0))] pointer-events-none" />

          {/* Ambient background light orbs (Desktop only) */}
          <div className="hidden md:block absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[850px] h-[500px] bg-blue-600/12 dark:bg-blue-600/20 rounded-full blur-[140px] pointer-events-none" />
          <div className="hidden md:block absolute bottom-1/3 right-1/4 w-[600px] h-[400px] bg-indigo-600/10 dark:bg-indigo-600/15 rounded-full blur-[130px] pointer-events-none" />
          <div className="hidden md:block absolute top-2/3 left-1/4 w-[500px] h-[350px] bg-sky-500/8 dark:bg-sky-500/12 rounded-full blur-[130px] pointer-events-none" />
        </div>

        {/* 2. Top Navigation Bar (Mobile Header vs Desktop Header) */}
        <MobileTopBar
          currentView={currentView}
          setCurrentView={setCurrentView}
          isConnected={isConnected}
        />

        <div className="hidden md:block">
          <Header
            currentView={currentView}
            setCurrentView={setCurrentView}
            isConnected={isConnected}
          />
        </div>

        {/* 3. Main Content Area with Bottom Bar clearance on Mobile */}
        <div className="flex-1 relative flex flex-col pt-16 sm:pt-24 pb-20 md:pb-0">
          <main className="flex-1 max-w-7xl w-full mx-auto px-3.5 sm:px-6 lg:px-8 py-0 relative z-10">
            <AnimatePresence mode="wait">
              {currentView === 'landing' && (
                <motion.div
                  key="landing-view"
                  initial={pageTransition.initial}
                  animate={pageTransition.animate}
                  exit={pageTransition.exit}
                  transition={pageTransition.transition}
                >
                  {/* Mobile Native Home Dashboard (<768px) */}
                  <div className="md:hidden">
                    <MobileHomeView
                      onStartAnalysis={() => setCurrentView('upload')}
                      onViewResults={() => setCurrentView('analysis')}
                      activeJobId={activeJobId}
                      isConnected={isConnected}
                    />
                  </div>

                  {/* Desktop Editorial Landing Page (>=768px) */}
                  <div className="hidden md:block">
                    <LandingView onStartAnalysis={() => setCurrentView('upload')} />
                  </div>
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

              {currentView === 'analysis' && !activeJobId && (
                <motion.div
                  key="empty-analysis-view"
                  initial={pageTransition.initial}
                  animate={pageTransition.animate}
                  exit={pageTransition.exit}
                  transition={pageTransition.transition}
                  className="text-center py-16 px-4 space-y-4 max-w-md mx-auto"
                >
                  <div className="w-14 h-14 rounded-2xl bg-blue-500/10 text-blue-500 mx-auto flex items-center justify-center border border-blue-500/20">
                    <img src="/logo.png" alt="CODEXA" className="w-8 h-8 object-contain" />
                  </div>
                  <h3 className="text-base font-bold font-display text-[var(--text-primary)]">
                    No Active Audit
                  </h3>
                  <p className="text-xs text-slate-500 leading-relaxed">
                    Upload a ZIP archive or enter a GitHub repository URL to start an automated AI code audit.
                  </p>
                  <button
                    onClick={() => setCurrentView('upload')}
                    className="cdx-btn-primary py-2.5 px-5 rounded-xl text-xs font-bold font-display shadow-md shadow-blue-500/25 cursor-pointer"
                  >
                    Start Codebase Audit
                  </button>
                </motion.div>
              )}

              {currentView === 'settings' && (
                <motion.div
                  key="settings-view"
                  initial={pageTransition.initial}
                  animate={pageTransition.animate}
                  exit={pageTransition.exit}
                  transition={pageTransition.transition}
                >
                  <MobileSettingsView isConnected={isConnected} limits={limits} />
                </motion.div>
              )}
            </AnimatePresence>
          </main>
        </div>

        {/* 4. Desktop Footer (Hidden on Mobile for Native App Look) */}
        <div className="hidden md:block">
          <Footer />
        </div>

        {/* 5. Traditional Native Mobile Bottom Navigation Bar (<768px) */}
        <BottomNav
          currentView={currentView}
          setCurrentView={setCurrentView}
          activeJobId={activeJobId}
        />
      </div>
    </>
  );
}

export default function App() {
  return (
    <ThemeProvider>
      <MainApp />
    </ThemeProvider>
  );
}
