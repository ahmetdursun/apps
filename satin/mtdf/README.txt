This Awari implementation uses the MTDf algorithm to do a forward search
through the possible game states. A transposition table is used to avoid
searching identical positions multiple times. The parallel version
speculatively searches multiple states in parallel, and replicates
(a part of) the transposition table to avoid search overhead.
Message combining is used to aggregate multiple transposition table
updates into a singe network message, to avoid excessive communication.
