public class Declansion {
    public static String declantion_of_word(int quantity){
        String result;
        if(quantity == 0 || quantity == 1 || quantity>=5)
            return "человек";
        else
            return "человека";

    }
}
