import java.util.Scanner;

public class datasheetGenerator{
   public static void main(String[] args){
      String[] allNotesSharps = {"C", "C\\sharp", "D", "D\\sharp", "E", "F", "F\\sharp", "G", "G\\sharp", "A", "A\\sharp", "B"};
      String[] allNotesFlats  = {"C", "D\\flat",  "D", "E\\flat",  "E", "F", "G\\flat",  "G", "A\\flat", "A",  "B\\flat" , "B"};
      String[] numbers = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Bonus"};
      String output = "";

      Note[] scale = new Note[8]; // the major scale with the target notes students are attempting to play
      System.out.println("Enter the note and note number of the scale attempted. Use \\flat and \\sharp to indicate accidentals. An example of a valid input is \"E\\flat3\" without the quotes.");
      String stdin;
      Scanner in = new Scanner(System.in); 
      
      stdin = in.nextLine();
      
      stdin = stdin.replace(" ","").replace("_",""); // if you downloaded this from Github and you like to use underscores or spaces when you type note names, this saves you the trouble of changing that habit
      
      String startingNote = stdin.substring( 0, stdin.length() - 1); // replace with input.nextLine();
      int    noteNumber   = Integer.parseInt( stdin.substring(stdin.length() - 1) );

      // does this key signature use sharps or flats?
      boolean useSharps = (startingNote.equals("C")) || (startingNote.equals("G")) || (startingNote.equals("D")) ||
                          (startingNote.equals("A")) || (startingNote.equals("E")) || (startingNote.equals("B")) || 
                          (startingNote.equals("F\\sharp")) || (startingNote.equals("C\\sharp"));
      
      // allNotes will be the array we use to generate the scale dynamically, so that we don't have to hard-code every possible major scale
      String[] allNotes = allNotesFlats;
      if (useSharps) {
         allNotes = allNotesSharps;
      }

      int[] hsIndexes = {0, 2, 4, 5, 7, 9, 11, 12}; // list of half steps between notes
      int scaleIndex = 0; // index of the Note array scale
      int majorIndex = Note.indexOf(startingNote, allNotes); // index of the letter note in allNotes
      for (int k : hsIndexes){
         if (scaleIndex == 4){ // fifth note (or index four) should have an octave jump
            noteNumber++;
         }

         // if we pass C, we increment our note number, but don't do it when we start on C or C sharp, and don't do it when we have a C sharp or D flat that immediately follows a C
         if((allNotes[(k + majorIndex) % 12].equals("C") || allNotes[(k + majorIndex) % 12].equals("C\\sharp") ||
             allNotes[(k + majorIndex) % 12].equals("D\\flat")) && scaleIndex != 0 && !(scale[scaleIndex - 1].letter().equals("C"))
           ) { noteNumber++; }

         scale[scaleIndex] = new Note(allNotes[(k + majorIndex) % 12], noteNumber);
         System.out.println(scale[scaleIndex].toString() + "   " + Double.toString(scale[scaleIndex].frequency()) + " Hz");
         scaleIndex++;  
      }

      if (scale[0].frequency() < 87 || scale[0].frequency() > 175){
         System.out.println("*********************\nWARNING! The starting note is not within the pitch range given in the rules.\n*********************");
      }

      System.out.println("Enter the bonus note with number. If no bonus note, type \"none\" or \"n/a\" without the quotes.");

      stdin = in.nextLine().replace(" ","").replace("_",""); 
      
      Note   bonus = null;
      String bonusLetter;
      int    bonusNumber;
      if( stdin.toLowerCase().indexOf("n") == -1 ){ // accept anything with an "n", in case I make a typo
         bonusLetter = stdin.substring( 0, stdin.length() - 1); // replace with input.nextLine();
         bonusNumber = Integer.parseInt( stdin.substring(stdin.length() - 1) );
         bonus       = new Note(bonusLetter, bonusNumber);
      }

      // create the LaTeX output for the notes of the scale
      for(int k = 0; k < scale.length; k++){ 
         output += "\\def\\note" + numbers[k] + "{" + scale[k].toString() + "}\n";
      }
      if (bonus != null) { output += "\\def\\noteBonus{" + bonus.toString() + "}\n\n"; }
      else { output += "\\def\\noteBonus{skipped}\n\n"; }
      for(int k = 0; k < scale.length; k++){
         // To round to the hundredths place, we use String.format("%.2f", someDouble)
         output += "\\def\\note" + numbers[k] + "Target{" + String.format("%.2f",scale[k].frequency()) + " Hz}\n";
      }
      if (bonus != null){ output += "\\def\\noteBonusTarget{" + String.format("%.2f",bonus.frequency()) + "}\n\n"; }
      else { output += "\\def\\noteBonusTarget{skipped}\n\n"; }

      System.out.println("Enter the pitch, in Hz, of each note as measured in Praat or Audacity. Separate each note with a space or a new line. To enter a skipped note, put in three hyphens."); // actually, you can put in anything that isn't a number, not just hyphens
      stdin = ""; // reset the input from previous scanner usage

      int totalLines = 1;
      while(in.hasNextLine()) {
         String line = in.nextLine();
         stdin += line + " ";
         totalLines++;
         if (totalLines > 9){ break; }
         if (line.isEmpty()){ break; }
      }

      String[] freqArrayRaw = stdin.split(" ");  // raw input turned into an array
      String[] freqArray = new String[9];        // we have at most nine notes of input
      int i = 0;
      for(int k = 0; k < freqArrayRaw.length; k++){ // put first nine non-empty inputs into freqArray
         if( !(freqArrayRaw[k].isEmpty()) ){
            freqArray[i] = freqArrayRaw[k];
            i++;
         }
         if(i > 8){ break; } // maximum index of freqArray is 8
      }

      double[] centsArray = new double[9]; // the array of cent values

      for(int k = 0; k < 9; k++){
         String freqStr = freqArray[k]; // the frequency of the kth note input by the student, as a string
         try { // check whether the student input a number or something else
            double f = Double.parseDouble(freqStr);
            output  += "\\def\\note" + numbers[k] + "Actual{" + String.format("%.2f",f) + " Hz}\n";

            // for the normal notes in a major scale
            if (k != 8) { centsArray[k] = scale[k].centsOff(f); }
            // for the bonus note, which is at index 8 because it's the 9th note
            else{
               // check whether bonus note exists
               if (bonus != null){ centsArray[k] = bonus.centsOff(f); }
               // if it doesn't exist, we skip that note
               else{ centsArray[k] = Double.MAX_VALUE; }
            }
        } catch (Exception e1){ // if something else, then we skip that note
            output  += "\\def\\note" + numbers[k] + "Actual{skipped}\n";
            centsArray[k] = Double.MAX_VALUE; // a placeholder cent value for a skipped note, since centsArray by default is filled entirely with zeroes after being initialized
        }
      }
      output += "\n";
      // do it again, but generating the cent value output this time
      for(int k = 0; k < 9; k++){
         if(centsArray[k] == Double.MAX_VALUE){ 
            output += "\\def\\note" + numbers[k] + "Cents{skipped}\n";
         }
         else{
            String centsFormatted = String.format("%.1f",centsArray[k]);
            if (centsFormatted.substring(0, 1).equals("-")){
               centsFormatted = "$-$" + centsFormatted.substring(1);
               if (centsFormatted.equals("$-$0.0")){
                  centsFormatted = "0.0";
               }
            }
            if (centsFormatted.equals("1.0")){ // singular versus plural
               output += "\\def\\note" + numbers[k] + "Cents{" + centsFormatted + " cent off}\n";
            }
            else{
               output += "\\def\\note" + numbers[k] + "Cents{" + centsFormatted + " cents off}\n";
            }
         }
      }
      output += "\n";

      boolean setMaximum = false;
      for (int k = 0; k < 9; k++){
         if (centsArray[k] != Double.MAX_VALUE){ // if the note exists
            output += "\\def\\graph" + numbers[k] + "{(" + Integer.toString(k * 40) + "," + Note.centMapping(centsArray[k]) + ")}\n";
            if(centsArray[k] >= 300){
               setMaximum = true;
            }
         }
         else{ // if the note doesn't exist
            output += "\\def\\graph" + numbers[k] + "{}\n";
         }
      }

      // forces PGFplots to display a maximum, to prevent our visualization from being destroyed by extremely large values
      if (setMaximum){ 
         output += "\n\\def\\setMaximum{ymax = 6}\n\n";
      }
      else { output += "\n\\def\\setMaximum{}\n\n"; }

      // score the instrument, as well as give estimates for the other programs
      double pitchScore = 0;
      double audacityEstimate = 0;
      double pasciolyEstimate = 0;        // one-time compressed
      double pasciolyEstimateTripleC = 0; // three-times compressed

      double meanErrorAudacity = -0.174856135652284; // mean error of Audacity from Praat, in cents
      double meanErrorPAscioly = -4.155381354;
      double meanErrorPAsciolyTripleC = -3.47874315393969;

      /* old version that gives the wrong score
      for (double centsOff : centsArray){
         if(centsOff != Double.MAX_VALUE){
            pitchScore += Note.getIPS(centsOff, "invitational");
         }
      }*/
      for (int k = 0; k < centsArray.length; k++){ // let's go through our list of cent values
         double centsOff = centsArray[k];
         if(centsOff != Double.MAX_VALUE){ // if it's not a skipped note
            if ( k != centsArray.length - 1 ){
               pitchScore              += Note.getIPS(centsOff, "invitational");
               audacityEstimate        += Note.getIPS(centsOff + meanErrorAudacity, "invitational");
               pasciolyEstimate        += Note.getIPS(Math.round(centsOff + meanErrorPAscioly), "invitational");
               pasciolyEstimateTripleC += Note.getIPS(Math.round(centsOff + meanErrorPAsciolyTripleC), "invitational");
               // Math.round accounts for the fact that intermediate rounding is necessary for pascioly.org/sounds
            }
            else { // bonus note
               pitchScore              += Note.getBonus(centsOff, "invitational");
               audacityEstimate        += Note.getBonus(centsOff + meanErrorAudacity, "invitational");
               pasciolyEstimate        += Note.getBonus(Math.round(centsOff + meanErrorPAscioly), "invitational");
               pasciolyEstimateTripleC += Note.getBonus(Math.round(centsOff + meanErrorPAsciolyTripleC), "invitational");
            } 

            // change "invitational" to "invy" if you prefer to spell it that way, or to "states" or "nationals" or "nattys"
         }
      }

      output += "\\def\\pitchScore{" + String.format("%.4f",pitchScore) + "}\n";
      output += "\\def\\audacityEstimate{"   + String.format("%.4f",audacityEstimate       ) + "}\n";
      output += "\\def\\pasciolyEstimate{"   + String.format("%.4f",pasciolyEstimate       ) + "}\n";
      output += "\\def\\pasciolyEstimateTC{" + String.format("%.4f",pasciolyEstimateTripleC) + "}";

      System.out.println(output);
   }
}