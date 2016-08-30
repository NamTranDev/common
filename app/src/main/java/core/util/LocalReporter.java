package core.util;

import android.content.Context;
import android.os.Environment;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import core.base.BaseApplication;


@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class LocalReporter implements ReportSender {

    private static final long MAX_LOG_SIZE = 2 * 1024 * 1024; // 20MB
    private final Map<ReportField, String> reportMap = new HashMap<>();
    private FileWriter crashReportWriter = null;

    public LocalReporter() {
        createCrashReportWriter();
    }

    private void createCrashReportWriter() {
        try {
            File directory = new File(Environment.getExternalStorageDirectory()
                    .getPath()
                    + "/"
                    + BaseApplication.getContext().getPackageName()
                    .replace(".", "_"));
            File log;
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    log = new File(directory.getPath(), "log.txt");
                    if (log.createNewFile())
                        crashReportWriter = new FileWriter(log, true);
                }
            } else {
                log = new File(directory.getPath(), "log.txt");
                if (!log.exists())
                    if (!log.createNewFile())
                        return;
                    else {
                        if (log.length() >= MAX_LOG_SIZE)
                            if (log.delete())
                                if (!log.createNewFile())
                                    return;
                    }

                crashReportWriter = new FileWriter(log, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> refactor(Map<ReportField, String> report) {

        ReportField[] fields = ACRA.getConfig().customReportContent();
        if (fields.length == 0) {
            fields = ACRAConstants.DEFAULT_REPORT_FIELDS;
        }

        final Map<String, String> result = new HashMap<>(
                report.size());
        for (ReportField field : fields) {
            if (reportMap == null || reportMap.get(field) == null) {
                result.put(field.toString(), report.get(field));
            } else {
                result.put(reportMap.get(field), report.get(field));
            }
        }
        return result;
    }

    @Override
    public void send(Context context, CrashReportData errorContent)
            throws ReportSenderException {
        ActionTracker.closeWithCrashActionLog();
        if (crashReportWriter != null) {
            final Map<String, String> finalReport = refactor(errorContent);

            try {
                BufferedWriter buffer = new BufferedWriter(crashReportWriter);

                Set<Entry<String, String>> set = finalReport.entrySet();
                for (Entry<String, String> entry : set) {
                    buffer.append("[").append(entry.getKey()).append("]=").append(entry.getValue());
                }
                buffer.flush();
                buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}