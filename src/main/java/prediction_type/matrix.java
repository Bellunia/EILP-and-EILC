package prediction_type;


import java.util.Random;
import java.util.Scanner;

/**
 * Matrix calculator that adds, subtracts, multiplies, and performs hadamard product
 */
public class matrix {
    private int rows;
    private int columns;
    private int[][] elements;
    public matrix(int rows, int columns){
        this.rows = rows;
        this.columns = columns;
        elements = new int[rows][columns];
        populatematrix(-100,100);
    }

    public matrix(int[][] matrixArray){
        this.rows = matrixArray.length;
        this.columns = matrixArray[0].length;
        elements = new int[this.rows][this.columns];
        for(int i = 0; i<this.rows; i+=1){
            for(int j = 0; j<this.columns; j+=1){
                this.elements[i][j] = matrixArray[i][j];
            }
        }
    }

    private void populatematrix(int min, int max){
        Random rand = new Random();
        for(int i = 0; i<rows; i+=1){
            for(int j = 0; j<columns; j+=1){
                elements[i][j] = rand.nextInt(max-min) + min;
                //delete below
                //System.out.print(elements[i][j]+"  ");
            }
            //delete below
            //System.out.println();
        }
    }

    public matrix add(matrix mat){
        int[][] answer = new int[this.rows][this.columns];
        if(this.rows == mat.rows && this.columns == mat.columns){
            for(int i = 0; i<this.rows; i+=1){
                for(int j = 0; j<this.columns; j+=1){
                    answer[i][j] = this.elements[i][j] + mat.elements[i][j];
                    //delete below
                    //System.out.print("  "+answer[i][j]);
                }
                //delete bellow
                //System.out.println();
            }
            return new matrix(answer);
        } else {
            System.out.println("The matrices are different sizes!");
            return null;
        }
    }

    public matrix subtract(matrix mat){
        if(this.rows == mat.rows && this.columns == mat.columns){
            int[][] answer = new int[this.rows][this.columns];
            for(int i = 0; i<this.rows; i+=1){
                for(int j = 0; j<this.columns; j+=1){
                    answer[i][j] = this.elements[i][j] - mat.elements[i][j];
                    //delete below
                    //System.out.print("  "+answer[i][j]);
                }
                //delete below
                //System.out.println();
            }
            return new matrix(answer);
        } else {
            System.out.println("The matrices are different sizes!");
            return null;
        }
    }

    public matrix multiply(matrix mat){
        int[][] answer = new int[this.rows][mat.columns];
        if(this.columns == mat.rows){
            for(int i = 0; i<this.rows; i+=1){
                for(int j = 0; j<mat.columns; j+=1){
                    for(int k = 0; k<this.columns; k+=1){
                        answer[i][j] += this.elements[i][k]*mat.elements[k][j];

                    }
                    //delete below
                    //System.out.print(" "+answer[i][j]);
                }
                //delete below
                //System.out.println();
            }

            return new matrix(answer);
        } else {
            System.out.println("These matrices can't be multiplied!");
            return null;
        }
    }

    public matrix hadprod(matrix mat){
        int[][] answer = new int[this.rows][this.columns];
        if(this.rows == mat.rows && this.columns == mat.columns){
            for(int i = 0; i<this.rows; i+=1){
                for(int j = 0; j<this.columns; j+=1){
                    answer[i][j] = this.elements[i][j] * mat.elements[i][j];
                    //delete below
                    //System.out.print("  "+answer[i][j]);
                }
                //delete below
                //System.out.println();
            }
            return new matrix(answer);
        } else {
            System.out.println("The matrices are different sizes!");
            return null;
        }
    }

    public String toString(){
        String result = "";
        for(int[] i : this.elements){
            result += "[";
            for(int j : i){
                result +=  String.format(" % 6d ", j);;
            }
            result += " ]\n";
        }
        return result;
    }

    public static void main(String args[]){
        Scanner input = new Scanner(System.in);

        //for testing

      int[][] one = {{2,4},{1,3}};//,{5,3}};
      int[][] two = {{2,1},{5,3}};
      matrix mat1 = new matrix(one);
      matrix mat2 = new matrix(two);
      //*/

        //*
//        int rows1;
//        int columns1;
//        int rows2;
//        int columns2;
//        System.out.println("Enter the amount of rows for the first matrix");
//        rows1 = input.nextInt();
//        System.out.println("Enter the amount of columns for the first matrix");
//        columns1 = input.nextInt();
//        System.out.println("Enter the amount of rows for the second matrix");
//        rows2 = input.nextInt();
//        System.out.println("Enter the amount of columns for the second matrix");
//        columns2 = input.nextInt();
//        matrix mat1 = new matrix(rows1, columns1);
//        matrix mat2 = new matrix(rows2, columns2);
        System.out.println("First Matrix:\n"+mat1.toString()+"\nSecond Matrix:\n"+mat2.toString());
        //*/

        System.out.println("\nWhat would you like to do?\n1. Add"+
                "\n2. Subtract\n3. Multiply\n4. Hadamard Product\n5. Quit");
        int choice = input.nextInt();
        matrix result;

        switch (choice){
            case 1:
                result = mat1.add(mat2);
                if (result != null){
                    System.out.println(result.toString());
                }
                break;
            case 2:
                result = mat1.subtract(mat2);
                if (result != null){
                    System.out.println(result.toString());
                }
                break;
            case 3:
                result = mat1.multiply(mat2);
                if (result != null){
                    System.out.println(result.toString());
                }
                break;
            case 4:
                result = mat1.hadprod(mat2);
                if (result != null){
                    System.out.println(result.toString());
                }
                break;
            case 5:
                break;
            default:
                System.out.println("Invalid choice");
                break;
        }
    }
}
