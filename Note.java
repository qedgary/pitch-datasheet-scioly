import java.lang.Math;

public class Note {
   private String letter; // pitch class
   private int    number; // note number
   private double frequency;
   
   private String[] allNotesSharps = {"C","C\\sharp","D","D\\sharp","E","F","F\\sharp","G","G\\sharp","A","A\\sharp","B"};
   private String[] allNotesFlats  = {"C","D\\flat", "D","E\\flat", "E","F","G\\flat", "G","A\\flat", "A","B\\flat" ,"B"};

   /**
    * Creates a musical note, where middle C has number 4.
    * @param letter the pitch class (letter name) of the note
    * @param number the note number
    */
   public Note(String letter, int number){
      this.letter = letter;
      this.number = number;
      int halfStepsFromC = indexOf(letter, allNotesSharps);
      if (halfStepsFromC == -1){ // if the note isn't a sharp or natural, check the list of flats
         halfStepsFromC = indexOf(letter, allNotesFlats);
      }

      double middleC = 261.625565300599;
      this.frequency = middleC * Math.pow(2, 1.0/12 * halfStepsFromC) * Math.pow(2, number - 4);
   }
   public String letter(){
      return this.letter;
   }

   public int number(){
      return this.number;
   }

   public double frequency(){
      return this.frequency;
   }

   public double centsOff(String measuredFrequency){
      return centsOff(Double.parseDouble(measuredFrequency));
   }

   public double centsOff(double measuredFrequency){
      return 100 * Math.log(measuredFrequency / this.frequency) / (Math.log(2) / 12.0);
   }

   public boolean equals(Object obj){
      if (obj == null || getClass() != obj.getClass()) { // is this a Note object?
         return false;
      }
      else { // if this is a Note object, check whether the frequencies are equal
         Note note = (Note) obj;
         return note.frequency() == this.frequency; // enharmonically equivalent notes are considered equal here
      }
   }
   /**
    * Returns the note letter and number in a LaTeX-compatible string.
    * @return the string
    */
   public String toString(){
      return this.letter + "$_" + Integer.toString(this.number) + "$";
   }

   /**
    * a static method for finding the first index of an element of an array, mainly because I don't want to import external code but I don't want to write a for-loop each time I want to search for an index
    * @param element the element
    * @param arr the array to be searched
    * @return the index if the element exists, or -1 if it doesn't exist
    */
   public static int indexOf(Object element, Object[] arr){
      int index = -1;
      for(int k = 0; k < arr.length; k++){
         if(element.equals(arr[k])){
            index = k;
            break;
         }
      }
      return index;
   }

   /**
    * Given a cent error from a note, finds a radial representation for the note on the pitch profile. The pitch profile uses polar coordinates such that a pitch measurement with zero cents of error is located at <em>r</em> = 2. Notes that are sharp will have <em>r</em> greater than 2, while notes that are flat will ahve <em>r</em> less than 2.
    * @param centsOff the error of a note in cents.
    * @return the radius <em>r</em> of the note as represented on the pitch profile.
    */
   public static double centMapping(double centsOff){
      double r;
      if(centsOff <= 0){ // if the cent reading is negative or zero
         centsOff = -centsOff; // because positive numbers are easier to work with
         int hundredCeiling = ((int) centsOff) / 100 + 1; // ceiling function of centsOff, but to each multiple of 100 rather than to integers
         r = 4 * Math.pow(0.5,hundredCeiling) - (centsOff - (hundredCeiling - 1) * 100.0) / (50.0 * Math.pow(2, hundredCeiling));
      }
      else{ // if the cent reading is positive
         if (centsOff <= 300){ r = 2.0 + centsOff / 100; } // cent reading sufficiently small (no more than 300)
         else{ // if the cent reading is really big (more than 300), then we'll want to make the radial distance grow more slowly, so that a huge data point doesn't wreck our visualization. This is pretty common since some students may be off by exactly one octave (1200 cents)
            r = 5 + Math.log10((centsOff - 200) / 100);
         }
      }
      if(r < 0) {r = 0;} // negative values of r not allowed
      return r;
   }

   public static double getIPS(double centsOff, String level){
      level = level.substring(0,1).toLowerCase();
      centsOff = Math.abs(centsOff);
      double ips = 0;
      if (level.equals("r") || level.equals("i")){
         if (centsOff <= 10) { ips = 4.5; }
         else { ips = 5 - 0.05 * centsOff; }
      }
      else if (level.equals("s")){
         if (centsOff <= 7) { ips = 4.5; }
         else { ips = 5 - 0.1 * centsOff; }
      }
      else if (level.equals("n")) {
         if (centsOff <= 3) { ips = 4.5; }
         else { ips = 5 - 0.2 * centsOff; }
      }
      if (ips < 0){
         ips = 0;
      }
      return ips;
   }
}