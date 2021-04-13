package com.ubki.service;

import com.google.common.base.Strings;
import com.ubki.dao.TeamRepository;
import com.ubki.entity.Team;
import com.ubki.utils.FioUtils;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TeamConnectionService {

    private final TeamService teamService;

    public TeamConnectionService(TeamService teamService) {
        this.teamService = teamService;
    }

    public void saveProgress(String source) throws IOException, ParserConfigurationException, SAXException {
        String sevenZUrl = getSevenZUrl(source);
        URI url = URI.create(sevenZUrl);
        try (InputStream inputStream = url.toURL().openStream()) {
            copyInputStreamToFile(inputStream);
        }
        readSevenZFile();
    }

    // parse url from html page and search link(a href) with .7z
    private String getSevenZUrl(String source) throws IOException {
        Document doc = Jsoup.connect(source).get();
        Elements links = doc.select("a[href]");
        String zipLink = null;
        for (Element one : links) {
            if (one.attr("href").contains(".7z")) {
                zipLink = one.attr("href");
                System.out.println(zipLink);
            }
        }
        return zipLink;
    }

    // read  byte[] and put Object to collection
    private void readSevenZFile() throws IOException, ParserConfigurationException, SAXException {
        final Map<String, byte[]> entriesByName = new HashMap<>();
        SevenZFile sevenZFile = new SevenZFile(new File("zip.7z"));
        SevenZArchiveEntry entry = sevenZFile.getNextEntry();
        while ((entry = sevenZFile.getNextEntry()) != null) {
            // download only xls and xlsx
            if (entry.hasStream() && (entry.getName().contains("xls") || entry.getName().contains("xlsx"))) {
                entriesByName.put(entry.getName(), readFully(sevenZFile));
            }
        }
        sevenZFile.close();
        // удалить файл не забыть
        // try  convert byte[] to  content
        Set<Team> teams = new LinkedHashSet<>();
        //using map.entrySet() for iteration
        for (Map.Entry<String, byte[]> one : entriesByName.entrySet()) {
            try {
                // System.out.println("file name = " + one.getKey());
                InputStream in = new ByteArrayInputStream(one.getValue());
                Workbook workbook = null;
                if (one.getKey().toLowerCase().endsWith("xlsx")) {
                    workbook = new XSSFWorkbook(in);
                } else if (one.getKey().toLowerCase().endsWith("xls")) {
                    workbook = new HSSFWorkbook(in);
                } else {
                    // if file name !xlsx or !xls ignore
                    continue;
                }
                int numberOfSheets = workbook.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    //Get the  sheet from the workbook
                    Sheet sheet = workbook.getSheetAt(i);
                    //every sheet has rows, iterate over them
                    Iterator<Row> rowIterator = sheet.iterator();
                    int index = 1;
                    boolean isIndexTrue = false;
                    while (rowIterator.hasNext()) {
                        String fio = "";
                        String stringDate = "";
                        Date date = null;
                        //Get the row object
                        Row row = rowIterator.next();
                        Cell cellFio = row.getCell(index);
                        Cell cellBirthday = row.getCell(index + 2);
                        // check the cell type and process accordingly,
                        // cell.getColumnIndex() == 1 - fio ,
                        // cell.getColumnIndex() == 3 - birthday,
                        // table cell.getColumnIndex() == 3, contains 2 format, String and Date
                        if (cellFio != null && Cell.CELL_TYPE_STRING == cellFio.getCellType() && !Strings.isNullOrEmpty(cellFio.getStringCellValue().trim())) {
                            if ("Прізвище, ім'я та по батькові".equalsIgnoreCase(cellFio.getStringCellValue().trim())) {
                                continue;
                            }
                            fio = cellFio.getStringCellValue().trim();
                        }
                        if (cellBirthday != null && Cell.CELL_TYPE_STRING == cellBirthday.getCellType() && !Strings.isNullOrEmpty(cellBirthday.getStringCellValue().trim())) {
                            stringDate = cellBirthday.getStringCellValue().trim();
                        }
                        if (cellBirthday != null && Cell.CELL_TYPE_NUMERIC == cellBirthday.getCellType()) {
                            date = cellBirthday.getDateCellValue();
                            stringDate = new SimpleDateFormat("dd.MM.yyyy").format(date);
                        }
                        if (!Strings.isNullOrEmpty(fio) && !Strings.isNullOrEmpty(stringDate) && FioUtils.fio(fio)) {
                            teams.add(new Team(fio, stringDate));
                        }
                    } //end of rows iterator
                } //end of sheets for loop
//                System.out.println("file name = " + one.getKey());
//                System.out.println("teams = " + teams.size());
                //close file input stream
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }
        teamService.saveEmployees(teams);
    }


    private byte[] readFully(SevenZFile archive) throws IOException {
        final byte[] buf = new byte[1024];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int len = 0; (len = archive.read(buf)) > 0; ) {
            baos.write(buf, 0, len);
        }
        return baos.toByteArray();
    }

    // save  to file name "zip.7z"
    private void copyInputStreamToFile(InputStream inputStream)
            throws IOException {
        File file = new File("zip.7z");
        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[11024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }
}
