"use client";

import React, { useEffect, useMemo, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";

export const AnimatedList = React.memo(
  ({ className, children, delay = 200 }) => {
    const childrenArray = React.Children.toArray(children);
    const [index, setIndex] = useState(0);

    useEffect(() => {
      setIndex(0);
    }, [childrenArray.length]);

    useEffect(() => {
      if (index < childrenArray.length - 1) {
        const timeout = setTimeout(() => {
          setIndex((prevIndex) => prevIndex + 1);
        }, delay);

        return () => clearTimeout(timeout);
      }
    }, [index, delay, childrenArray.length]);

    const itemsToShow = useMemo(
      () => childrenArray.slice(0, index + 1),
      [index, childrenArray]
    );

    return (
      <div className={`flex flex-col gap-3 ${className || ""}`}>
        <AnimatePresence mode="popLayout">
          {itemsToShow.map((item, idx) => (
            <motion.div
              key={item.key || idx}
              initial={{ scale: 0.9, opacity: 0, y: 15 }}
              animate={{ scale: 1, opacity: 1, y: 0 }}
              exit={{ scale: 0.9, opacity: 0, y: -15 }}
              transition={{ type: "spring", stiffness: 400, damping: 28 }}
              layout
              className="w-full"
            >
              {item}
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    );
  }
);

AnimatedList.displayName = "AnimatedList";

export default AnimatedList;
