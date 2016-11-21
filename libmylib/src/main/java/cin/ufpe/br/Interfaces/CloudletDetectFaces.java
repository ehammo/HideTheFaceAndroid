package cin.ufpe.br.Interfaces;

import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import java.io.File;
import java.util.List;

import cin.ufpe.br.model.PropriedadesFace;

/**
 * Created by eduardo on 31/10/2016.
 */

public interface CloudletDetectFaces extends DetectFaces {
    List<PropriedadesFace> detectarFaces(String cascadeClassifier, byte[] originalImage);

   // List<PropriedadesFace> obterDadosFaces(MatOfRect matOfRect);
}
