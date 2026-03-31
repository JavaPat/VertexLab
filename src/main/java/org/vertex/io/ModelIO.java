package org.vertex.io;

import org.json.*;
import org.vertex.model.Face;
import org.vertex.model.Model3D;
import org.vertex.model.Vertex;

import java.io.*;
import java.nio.file.Files;

public class ModelIO {

    public static void save(Model3D model, File file) throws Exception {
        JSONObject root = new JSONObject();

        JSONArray verts = new JSONArray();
        for (Vertex v : model.vertices) {
            verts.put(new JSONArray(new float[]{v.x, v.y, v.z}));
        }

        JSONArray faces = new JSONArray();
        for (Face f : model.faces) {
            JSONObject obj = new JSONObject();
            obj.put("indices", f.indices);
            obj.put("color", String.format("#%06X", f.color));
            faces.put(obj);
        }

        root.put("vertices", verts);
        root.put("faces", faces);

        Files.writeString(file.toPath(), root.toString(2));
    }

    public static Model3D load(File file) throws Exception {
        String content = Files.readString(file.toPath());
        JSONObject root = new JSONObject(content);

        Model3D model = new Model3D();

        JSONArray verts = root.getJSONArray("vertices");
        for (int i = 0; i < verts.length(); i++) {
            JSONArray v = verts.getJSONArray(i);
            model.addVertex(
                    (float) v.getDouble(0),
                    (float) v.getDouble(1),
                    (float) v.getDouble(2)
            );
        }

        JSONArray faces = root.getJSONArray("faces");
        for (int i = 0; i < faces.length(); i++) {
            JSONObject f = faces.getJSONObject(i);

            JSONArray idxArr = f.getJSONArray("indices");
            int[] indices = new int[idxArr.length()];
            for (int j = 0; j < idxArr.length(); j++) {
                indices[j] = idxArr.getInt(j);
            }

            int color = Integer.parseInt(
                    f.getString("color").substring(1), 16
            );

            model.addFace(indices, color);
        }

        return model;
    }
}
