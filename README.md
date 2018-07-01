# Set-Relations-Program
This program takes a binary relation on a set and outputs the relation's properties, such as reflexiveness, transitivity, symmetry, etc. It inputs and outputs the relations as an N by N boolean matrix.

The professor only required us to make it accept 8x8 matricies, but I figured it wouldn't hurt to make it accept any size. I actually had a lot of fun working on this project, as well as trying to come up with more efficient algorithms to check various properties.

One thing I would do differently, however, is that I intentionally used Boolean wrapper variables instead of primitive booleans so that I could effectively have them use three states: true, false, and null, such that I can save the calculated properties and know which ones have or have not been calculated yet. In hindsight, this is kind of an ugly way of handling things, at least from the computer's point of view. It probably would have been better to just use two boolean primitives for each property, one to record its value, and another to record if the property has been computed yet.
