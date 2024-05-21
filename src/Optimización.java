public class Optimización {
    public static void main(String[] args) {
        long tiempoIni = System.nanoTime();
        noOptimizado();
        long tiempoFini = System.nanoTime();
        long tiempoToti = tiempoFini - tiempoIni;
        System.out.println("Tiempo de Ejecución No optimizado: " + tiempoToti);
        tiempoIni = System.nanoTime();
        optimizado();
        tiempoFini = System.nanoTime();
        tiempoToti = tiempoFini - tiempoIni;
        System.out.println("Tiempo de Ejecución Optimizado: " + tiempoToti);
    }

    public static void noOptimizado(){
        float a, b, c, d;
        b = 100.76f;
        for(int i=0; i<1050; i++){
            a =(b * 3000.2f/2 + 40) * 150 / 11;
            c =(b * 3000.2f/2 + 40) * 320 / 290;
            d =(b * 3000.2f/2 + 40) * 100 / 3000;
        }
    }

    public static void optimizado(){
        float a, b, c, d;
        b = 100.76f;
        float temp = b * 3000.2f/2 + 40;
        for(int i=0; i<1050; i++){
            a =temp * 150 / 11;
            c =temp * 320 / 290;
            d =temp * 100 / 3000;
        }
    }
}
