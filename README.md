# Clipper
Clipper takes an input audio clip, and restitches it into something completely new.


Clipper is a class that creates AudioInputStreams or Files by splitting an input file. These split clips 
can then be sequenced for playback, or recorded.

Here is an example of how to use Clipper in a driver:

    public Driver {
      public static void main (String[] args){
        // The given file inputs need to be altered. 
        Clipper amen = new Clipper("Amen", "C:/Users/mattc/Documents/Code 2019/Java/OneToTheFour/src/onetothefour/Amen-Break.wav",
        "C:/Users/mattc/Documents/Code 2019/Java/OneToTheFour/src/onetothefour/");

        String sequence[] = {"e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8"};

        AudioInputStream test = amen.stitch(sequence, false);
      }
    }
    
The Clipper object amen is initialized with a title, sample filepath, and an output filepath, if needed.
The sequence[] string array is used to sequence the sample. 

Sequence arrays must have at least two clips, and less than 12. The sequence inputs are simple:

e1, e2, ..., e8 are eight even clips of the input file.
q1, q2, q3, and q4 are four even clips of the input file.

Clipper is helpful for simply slicing up samples, or resequencing them in quick and atypical ways.
It works great on vocal chops and drum breaks.
