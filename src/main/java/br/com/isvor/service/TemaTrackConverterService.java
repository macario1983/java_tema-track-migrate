package br.com.isvor.service;

import br.com.isvor.model.config.DbConnectionData;
import br.com.isvor.model.config.File;
import br.com.isvor.model.entity.TemaTrackResponse;
import br.com.isvor.model.mapper.TemaTrack;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class TemaTrackConverterService {

    private final Properties properties;
    private final DbConnectionData dbConnectionData;
    private final File fileJson;
    private final File fileSql;

    public TemaTrackConverterService() {
        properties = new Properties();
        dbConnectionData = new DbConnectionData();
        fileJson = new File();
        fileSql = new File();
    }

    public void execute() {
        loadProperties();
        readJsonFile(fileJson.getPath());
    }

    private void loadProperties() {
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            fileJson.setPath(properties.get("file.json").toString());
            fileSql.setPath(properties.get("file.sql").toString());
            dbConnectionData.setUrl(properties.get("db.url").toString());
            dbConnectionData.setPort(properties.get("db.port").toString());
            dbConnectionData.setUser(properties.get("db.user").toString());
            dbConnectionData.setPassword(properties.get("db.password").toString());
            dbConnectionData.setSchema(properties.get("db.schema").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJsonFile(String jsonFileURI) {

        System.out.println("*****************************************************************************************");
        System.out.println("MIGRAÇÃO DE DADOS");
        System.out.println("*****************************************************************************************");

        String json = null;

        try {
            json = new String(Files.readAllBytes(Paths.get(jsonFileURI)));
            System.out.println("Arquivo carregado...");
            getResponses(json);
        } catch (IOException ex) {
            System.out.println("Arquivo não encontrado!");
            System.exit(1);
        }

        if (json == null) {
            System.out.println("Arquivo não encontrado!");
            System.exit(1);
        }
    }

    private void getResponses(String json) {

        try {

            System.out.println("Objetos criados...");
            json = threatJson(json);
            List<TemaTrackResponse> response = new ObjectMapper().readValue(json, new TypeReference<List<TemaTrackResponse>>() {
            });
            List<TemaTrack> temaTracks = convertTemaTracksResponseToTemaTracks(response);
            generateSqlFile(temaTracks);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private String threatJson(String json) {
        return json.replace("ObjectId(", "").replace(")", "");
    }

    private List<TemaTrack> convertTemaTracksResponseToTemaTracks(List<TemaTrackResponse> temaTracksResponse) {

        System.out.println("Quantidade de tracks: " + temaTracksResponse.size());
        System.out.println("*****************************************************************************************");

        return temaTracksResponse
                .stream()
                .map(temaTrackResponse -> TemaTrack
                        .builder()
                        .profissionalId(temaTrackResponse.getProfissionalId())
                        .temaId(temaTrackResponse.getTemaId())
                        .progresso(temaTrackResponse.getTemaTracking())
                        .build())
                .collect(Collectors.toList());
    }

    private void generateSqlFile(List<TemaTrack> temaTracks) {

        try {

            StringBuilder stringBuilder = new StringBuilder();

            for (TemaTrack temaTrack : temaTracks) {

                LocalDateTime now = LocalDateTime.now();

                String sql = String.format("INSERT INTO tema_track (tema_id_integracao, profissional_id_integracao, progresso, data_criacao, data_alteracao, versao, ativo) " +
                                "VALUES(%s, %s, %s, %s, %s, %s, %s);",
                        addQuote(temaTrack.getTemaId()),
                        addQuote(temaTrack.getProfissionalId()),
                        addQuote(temaTrack.getProgresso()),
                        addQuote(now.toString()),
                        addQuote(now.toString()),
                        (short) 1,
                        (short) 1);

                stringBuilder.append(sql);
                stringBuilder.append(System.getProperty("line.separator"));
            }

            Files.write(Paths.get(fileSql.getPath()), stringBuilder.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.CREATE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String addQuote(String value) {

        if (value == null) {
            return null;
        }

        return '"' + value.trim() + '"';
    }

    private Connection getConnection() throws SQLException {

        try {
            String urlConnection = dbConnectionData.toString();
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(urlConnection, dbConnectionData.getUser(), dbConnectionData.getPassword());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
