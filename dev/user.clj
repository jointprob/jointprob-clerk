(require '[nextjournal.clerk :as clerk])

;; start Clerk's built-in webserver on the default port 7777, opening the browser when done
(clerk/serve! {:watch-paths ["notebooks" "src"]})


(comment
  (require '[nextjournal.clerk :as clerk])
  ;; to require tests:
  (require '[dbinomial-test])
  (clerk/serve! {:watch-paths ["notebooks" "src"]})
  ;; start without file watcher, open browser when started
  (clerk/serve! {:browse? true})

  ;; start with file watcher for these sub-directory paths
  (clerk/serve! {:watch-paths ["notebooks" "src" "index.md"]})

  ;; start with file watcher and a `show-filter-fn` function to watch
  ;; a subset of notebooks
  (clerk/serve! {:watch-paths ["notebooks" "src"] :show-filter-fn #(clojure.string/starts-with? % "notebooks")})
  (clerk/serve! {:browse? false})
  (clerk/halt-watcher!)
  (clerk/clear-cache!)
  (clerk/halt!)

  ;; or call `clerk/show!` explicitly
  (clerk/show! "notebooks/bayes_posterior.clj")
  (clerk/show! "notebooks/bayes_posterior_static.clj")
  (clerk/show! "notebooks/notes_further_to_stat_rethink.clj")
  (clerk/show! "notebooks/making_the_model_go.clj")
  (clerk/show! "notebooks/ch3_code_snippets.clj")
  (clerk/show! "notebooks/expected_loss.clj")
  (clerk/show! "notebooks/howell1.clj")


  (clerk/show! "index.md")

  ;; TODO If you would like more details about how Clerk works, here's a
  ;; notebook with some implementation details.
  ;; (clerk/show! "notebooks/how_clerk_works.clj")

  ;; produce a static app
  (clerk/build-static-app! {:paths (into ["index.md"]
                                         (mapv #(str "notebooks/" % ".clj")
                                               '[introduction data_science rule_30 semantic]))})

  (clerk/build-static-app! {:paths ["notebooks/**"] :bundle? false}))
