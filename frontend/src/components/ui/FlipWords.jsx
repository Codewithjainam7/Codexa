"use client";
import React, { useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { cn } from '../../lib/utils';

export const FlipWords = ({
  words = ['better', 'cute', 'beautiful', 'modern'],
  duration = 2800,
  className = '',
}) => {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setIndex((prevIndex) => (prevIndex + 1) % words.length);
    }, duration);
    return () => clearInterval(interval);
  }, [words, duration]);

  const currentWord = words[index];

  return (
    <span className="inline-flex overflow-hidden align-middle my-auto h-[1.25em]">
      <AnimatePresence mode="wait" initial={false}>
        <motion.span
          key={currentWord}
          initial={{ y: "100%", opacity: 0, filter: "blur(6px)" }}
          animate={{ y: "0%", opacity: 1, filter: "blur(0px)" }}
          exit={{ y: "-100%", opacity: 0, filter: "blur(6px)" }}
          transition={{
            y: { type: "spring", stiffness: 140, damping: 18 },
            opacity: { duration: 0.25 },
            filter: { duration: 0.2 }
          }}
          className={cn(
            "inline-block whitespace-nowrap leading-none",
            className
          )}
        >
          {currentWord}
        </motion.span>
      </AnimatePresence>
    </span>
  );
};

export default FlipWords;
