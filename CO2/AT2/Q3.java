import java.util.*;
class Main{
    public static void main(String[] args){
        Scanner s=new Scanner(System.in);
        String str=s.nextLine();
        String rev="";
        for(int i=0;i<str.length();i++)
            rev=str.charAt(i)+rev;
        System.out.println(rev);
    }
}