This directory contains a simple text compressor that uses backreferences
to previous text as compression method. It looks ahead for a number of
compression steps to select the best sequence of backreferences.

usage:

Compress <input-file> <output-file>

The program takes the following command-line options:

    -depth <n>  The number of levels of look-ahead used to determine the
                best way to compress the text.

    -short <n>  Consider shortening the text by at most <n> bytes to
                get a better subsequent backreference.

    -verify     After compressing the text, decompress it again, and compare
                against the original to make sure the compression was correct.

Beware that some types of text (e.g. source code) have many repeats,
and may require lower values for the depth and short parameters for
acceptable execution times.


There is also a de-compressor:

Decompress <input-file> <output-file>

