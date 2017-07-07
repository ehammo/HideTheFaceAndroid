package br.ufpe.cin.mpos.profile;

/**
 * Created by eduardo on 28/05/2017.
 */

public class ResultTypes {

    public enum ResultTypesApps {
        BenchFace, BenchImage, CollisionBalls
    }

    public enum ResultTypesPhone {
        Potente, Intermediario_Avancado, Basico, Fraco, Intermediario
    }

    public enum ResultTypesBateria {
        Forte, Boa, Razoavel, Fraca
    }

    public enum ResultTypesCpu {
        Relaxado, Carga_Normal, Estressado, Desconhecido
    }

    public enum ResultTypesRede{
        Wifi, CDMA, iDen, eHRPD, EDGE, GPRS, UMTS, EVDO, HSPA, HSDPA, HSUPA, LTE, Outro
    }

    public enum ResultTypesLarguraRede {
        Livre, Mediano, Congestionado
    }

    public enum ResultTypesRSSI {
        Otimo, Bom, Pobre, Sem_Sinal
    }

    public enum ResultTypesResult {
        Sim,Nao
    }



}