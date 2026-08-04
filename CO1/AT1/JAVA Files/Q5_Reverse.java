public class Q5_Reverse{
public static void main(String[]args){
String str="Hello";
String rev="";
for(int i=0;i<str.length();i++){
rev=str.charAt(i)+rev;
}
System.out.println(rev);
}
}