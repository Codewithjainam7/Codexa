import React, { useCallback, useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { cn } from '../../lib/utils';

export const FlipWords = ({
  words = ['better', 'cute', 'beautiful', 'modern'],
  duration = 2600,
  className = '',
}) => {
  const [currentWord, setCurrentWord] = useState(words[0]);
  const [isAnimating, setIsAnimating] = useState(false);

  const startAnimation = useCallback(() => {
    const nextIndex = (words.indexOf(currentWord) + 1) % words.length;
    const nextWord = words[nextIndex] || words[0];
    setCurrentWord(nextWord);
    setIsAnimating(true);
  }, [currentWord, words]);

  useEffect(() => {
    if (!isAnimating) {
      const timer = setTimeout(() => {
        startAnimation();
      }, duration);
      return () => clearTimeout(timer);
    }
  }, [isAnimating, duration, startAnimation]);

  return (
    <AnimatePresence
      mode="wait"
      onExitComplete={() => {
        setIsAnimating(false);
      }}
    >
      <motion.span
        key={currentWord}
        initial={{
          opacity: 0,
          y: 12,
          filter: 'blur(4px)',
        }}
        animate={{
          opacity: 1,
          y: 0,
          filter: 'blur(0px)',
        }}
        transition={{
          type: 'spring',
          stiffness: 120,
          damping: 14,
        }}
        exit={{
          opacity: 0,
          y: -18,
          filter: 'blur(6px)',
          transition: { duration: 0.2 },
        }}
        className={cn(
          'z-10 inline-block relative text-left whitespace-nowrap',
          className
        )}
      >
        {currentWord.split(' ').map((word, wordIndex) => (
          <span key={word + wordIndex} className="inline-block whitespace-nowrap">
            {word.split('').map((letter, letterIndex) => (
              <motion.span
                key={word + letterIndex}
                initial={{ opacity: 0, y: 8, filter: 'blur(4px)' }}
                animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
                transition={{
                  delay: wordIndex * 0.15 + letterIndex * 0.025,
                  duration: 0.2,
                }}
                className="inline-block"
              >
                {letter}
              </motion.span>
            ))}
            <span className="inline-block">&nbsp;</span>
          </span>
        ))}
      </motion.span>
    </AnimatePresence>
  );
};

export default FlipWords;
