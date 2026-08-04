import java.util.*;
public class Main{
    public static void main(String[]args){
        Scanner s=new Scanner(System.in);
        int n=s.nextInt();
        String[]x=new String[n];
        for(int i=0;i<n;i++)x[i]=s.next();
        for(int i=0;i<n;i++){
            if(x[i]==null)continue;
            int c=1;
            for(int j=i+1;j<n;j++){
                if(x[j]!=null&&x[i].equals(x[j])){
                    c++;
                    x[j]=null;
                }
            }
            if(c>1)System.out.print(x[i]+"="+c+" ");
        }
    }
}