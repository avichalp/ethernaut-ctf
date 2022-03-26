(ns ctf.core)


(defmacro try-async
  [bindings body-expr]
  `(cljs.core.async/go
     (try
       (let ~bindings ~body-expr)
       (catch js/Error err#
         (js/console.log (cljs.core/ex-cause err#))))))

(comment

  (macroexpand-1 '(try-async
                   [a 1
                    b 2]
                   (+ a b)))
  )
