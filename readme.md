These are just some experiments which helped me to get to grips both with using vegalite in Clerk notebooks (in clojure).
I was playing with the equations for calculating a binomial distribution in order to get a better grip on "bayesian updating".

Changes pushed to github will automatically be compiled by a github action and deployed to github pages you may need to enable this for your repository if you fork this one.

See built static pages [here.](https://jointprob.github.io/jointprob-clerk/)

Notebooks include:

* [A slideshow of notes I made to clarify my thinking about Statistical Rethinking Chapter 2.](https://jointprob.github.io/jointprob-clerk/notebooks/notes_further_to_stat_rethink.html)
* [An attempt at animating Bayesian updating](https://jointprob.github.io/jointprob-clerk/notebooks/bayes_posterior_static.html)
* [A slidehow of graphs showing a model adapting to evidence, basically the same as the above animated example but as a series of slides.](https://jointprob.github.io/jointprob-clerk/notebooks/bayes_posterior.html)

TODO:
* Others might be able to improve on my explanations in the first slideshow.
* Not happy with the symbol representing land and water sample W and L also being used for parameter name.
* The animation I tried to do in notebooks/bayes_posterior.clj doesn't work on github pages, want to experiment with cljs next.
* Structure of code could probably be improved.


![badge](https://img.shields.io/static/v1?label=Run%20with&message=Clerk&color=rgb(50,175,209)&logo=plex&logoColor=rgb(50,175,209))