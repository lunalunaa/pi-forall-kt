## Introduction
This project was started as a Kotlin implementation of **pi-forall**, a dependently typed language developed by
Stephanie Weirich for teaching purposes, first appeared at OPLSS 2014. However, more features (and many differences) are
to be expected in this implementation. So this project would eventually grow out of the original one.

The purpose of this project is for me to familiarize some of the implementation details of common features found in
dependently typed programming languages.

## Roadmap (subject to rapid change)

- [ ] Basic bidirectional elaborator for MLTT with one universe
  - [x] Terms
  - [x] Top-level definitions
  - [ ] Propositional equality
- [ ] Basic inductive data types and pattern matching
- [ ] Universe levels
  - [ ] universe polymorphism
- [ ] implicit arguments
- [ ] metavariables(holes)
- [ ] termination checking

## Reading List

These are the papers that I found relevant to implementing a dependently typed PL.

- Coquand, T., Kinoshita, Y., Nordström, B., & Takeyama, M. (2009). A simple type-theoretic language: Mini-TT. From
  Semantics to Computer Science; Essays in Honour of Gilles Kahn,
  139-164. https://www.cse.chalmers.se/~bengt/papers/GKminiTT.pdf
- Dunfield, J., & Krishnaswami, N. (2020). Bidirectional Typing. ArXiv:1908.05839 [Cs]. http://arxiv.org/abs/1908.05839
- Zhang, T. (2021). Elegant elaboration with function invocation. arXiv preprint arXiv:
  2105.14840. https://arxiv.org/abs/2105.14840
- Cockx, J., & Abel, A. (2020). Elaborating dependent (co)pattern matching: No pattern left behind. Journal of
  Functional Programming, 30, e2. https://doi.org/10.1017/S0956796819000182
- Abel, A. (1998). foetus–termination checker for simple functional programs. Programming Lab Report,
  474. https://www.cse.chalmers.se/~abela/foetus/
- Cockx, J., Devriese, D., & Piessens, F. (2016). Unifiers as equivalences: Proof-relevant unification of dependently
  typed data. Acm Sigplan Notices, 51(9), 270-283. https://dl.acm.org/doi/10.1145/2951913.2951917
- Abel, A., & Altenkirch, T. (2002). A predicative analysis of structural recursion. Journal of functional programming,
  12(1), 1-41. https://dl.acm.org/doi/10.1017/S0956796801004191
- Kovács, A. (2020). Elaboration with first-class implicit function types. Proceedings of the ACM on Programming
  Languages, 4(ICFP), 1-29. https://dl.acm.org/doi/abs/10.1145/3408983