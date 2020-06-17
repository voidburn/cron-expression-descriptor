package it.burning.cron;

import it.burning.cron.CronExpressionParser.Options;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CronExpressionDescriptorTest {
    @Test
    void getDescription() {
        // Default options
        final Options defaultOptions = new Options();

        // Test examples taken from (http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#examples)
        assertAll("Descriptions should match the expected results",
                  () -> assertEquals("At 12:00, every day", CronExpressionDescriptor.getDescription("0 0 12 * * ?", defaultOptions)),
                  () -> assertEquals("At 10:15, every day", CronExpressionDescriptor.getDescription("0 15 10 ? * *", defaultOptions)),
                  () -> assertEquals("At 10:15, every day", CronExpressionDescriptor.getDescription("0 15 10 * * ?", defaultOptions)),
                  () -> assertEquals("At 10:15, every day", CronExpressionDescriptor.getDescription("0 15 10 * * ? *", defaultOptions)),
                  () -> assertEquals("At 10:15, every day, only in 2005", CronExpressionDescriptor.getDescription("0 15 10 * * ? 2005", defaultOptions)),
                  () -> assertEquals("Every minute, between 14:00 and 14:59, every day", CronExpressionDescriptor.getDescription("0 * 14 * * ?", defaultOptions)),
                  () -> assertEquals("Every 5 minutes, between 14:00 and 14:59, every day", CronExpressionDescriptor.getDescription("0 0/5 14 * * ?", defaultOptions)),
                  () -> assertEquals("Every 5 minutes, at 14:00 and 18:00, every day", CronExpressionDescriptor.getDescription("0 0/5 14,18 * * ?", defaultOptions)),
                  () -> assertEquals("Every minute between 14:00 and 14:05, every day", CronExpressionDescriptor.getDescription("0 0-5 14 * * ?", defaultOptions)),
                  () -> assertEquals("At 10 and 44 minutes past the hour, at 14:00, every day, only on Wednesday, only in March", CronExpressionDescriptor.getDescription("0 10,44 14 ? 3 WED", defaultOptions)),
                  () -> assertEquals("At 10:15, every day, Monday through Friday", CronExpressionDescriptor.getDescription("0 15 10 ? * MON-FRI", defaultOptions)),
                  () -> assertEquals("At 10:15, on day 15 of the month", CronExpressionDescriptor.getDescription("0 15 10 15 * ?", defaultOptions)),
                  () -> assertEquals("At 10:15, on the last day of the month", CronExpressionDescriptor.getDescription("0 15 10 L * ?", defaultOptions)),
                  () -> assertEquals("At 10:15, 2 days before the last day of the month", CronExpressionDescriptor.getDescription("0 15 10 L-2 * ?", defaultOptions)),
                  () -> assertEquals("At 10:15, every day, on the last Friday of the month", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L", defaultOptions)),
                  () -> assertEquals("At 10:15, every day, on the last Friday of the month, 2002 through 2005", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L 2002-2005", defaultOptions)),
                  () -> assertEquals("At 10:15, every day, on the third Friday of the month", CronExpressionDescriptor.getDescription("0 15 10 ? * 6#3", defaultOptions)),
                  () -> assertEquals("At 12:00, every 5 days", CronExpressionDescriptor.getDescription("0 0 12 1/5 * ?", defaultOptions)),
                  () -> assertEquals("At 11:11, on day 11 of the month, only in November", CronExpressionDescriptor.getDescription("0 11 11 11 11 ?", defaultOptions))
        );
    }

    @Test
    void setDefaultLocale() {
        // Test setting a default locale
        CronExpressionDescriptor.setDefaultLocale("it");
        assertAll("Descriptions should match the expected results",
                  () -> assertEquals("Alle 12:00, ogni giorno", CronExpressionDescriptor.getDescription("0 0 12 * * ?")),
                  () -> assertEquals("Alle 10:15, ogni giorno", CronExpressionDescriptor.getDescription("0 15 10 ? * *")),
                  () -> assertEquals("Alle 10:15, ogni giorno", CronExpressionDescriptor.getDescription("0 15 10 * * ?")),
                  () -> assertEquals("Alle 10:15, ogni giorno", CronExpressionDescriptor.getDescription("0 15 10 * * ? *")),
                  () -> assertEquals("Alle 10:15, ogni giorno, solo nel 2005", CronExpressionDescriptor.getDescription("0 15 10 * * ? 2005")),
                  () -> assertEquals("Ogni minuto, tra le 14:00 e le 14:59, ogni giorno", CronExpressionDescriptor.getDescription("0 * 14 * * ?")),
                  () -> assertEquals("Ogni 5 minuti, tra le 14:00 e le 14:59, ogni giorno", CronExpressionDescriptor.getDescription("0 0/5 14 * * ?")),
                  () -> assertEquals("Ogni 5 minuti, alle 14:00 e 18:00, ogni giorno", CronExpressionDescriptor.getDescription("0 0/5 14,18 * * ?")),
                  () -> assertEquals("Ogni minuto tra le 14:00 e le 14:05, ogni giorno", CronExpressionDescriptor.getDescription("0 0-5 14 * * ?")),
                  () -> assertEquals("Al minuto 10 e 44 passata l'ora, alle 14:00, ogni giorno, solo il Mercoled\u00EC, solo in Marzo", CronExpressionDescriptor.getDescription("0 10,44 14 ? 3 WED")),
                  () -> assertEquals("Alle 10:15, ogni giorno, dal Luned\u00EC al Venerd\u00EC", CronExpressionDescriptor.getDescription("0 15 10 ? * MON-FRI")),
                  () -> assertEquals("Alle 10:15, il giorno 15 del mese", CronExpressionDescriptor.getDescription("0 15 10 15 * ?")),
                  () -> assertEquals("Alle 10:15, l'ultimo giorno del mese", CronExpressionDescriptor.getDescription("0 15 10 L * ?")),
                  () -> assertEquals("Alle 10:15, 2 giorni prima dell'ultimo giorno del mese", CronExpressionDescriptor.getDescription("0 15 10 L-2 * ?")),
                  () -> assertEquals("Alle 10:15, ogni giorno, l'ultimo Venerd\u00EC del mese", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L")),
                  () -> assertEquals("Alle 10:15, ogni giorno, l'ultimo Venerd\u00EC del mese, dal 2002 al 2005", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L 2002-2005")),
                  () -> assertEquals("Alle 10:15, ogni giorno, il terzo Venerd\u00EC del mese", CronExpressionDescriptor.getDescription("0 15 10 ? * 6#3")),
                  () -> assertEquals("Alle 12:00, ogni 5 giorni", CronExpressionDescriptor.getDescription("0 0 12 1/5 * ?")),
                  () -> assertEquals("Alle 11:11, il giorno 11 del mese, solo in Novembre", CronExpressionDescriptor.getDescription("0 11 11 11 11 ?"))
        );

        // Test resetting default locale
        CronExpressionDescriptor.setDefaultLocale(null);
        assertAll("Descriptions should match the expected results",
                  () -> assertEquals("At 12:00, every day", CronExpressionDescriptor.getDescription("0 0 12 * * ?")),
                  () -> assertEquals("At 10:15, every day", CronExpressionDescriptor.getDescription("0 15 10 ? * *")),
                  () -> assertEquals("At 10:15, every day", CronExpressionDescriptor.getDescription("0 15 10 * * ?")),
                  () -> assertEquals("At 10:15, every day", CronExpressionDescriptor.getDescription("0 15 10 * * ? *")),
                  () -> assertEquals("At 10:15, every day, only in 2005", CronExpressionDescriptor.getDescription("0 15 10 * * ? 2005")),
                  () -> assertEquals("Every minute, between 14:00 and 14:59, every day", CronExpressionDescriptor.getDescription("0 * 14 * * ?")),
                  () -> assertEquals("Every 5 minutes, between 14:00 and 14:59, every day", CronExpressionDescriptor.getDescription("0 0/5 14 * * ?")),
                  () -> assertEquals("Every 5 minutes, at 14:00 and 18:00, every day", CronExpressionDescriptor.getDescription("0 0/5 14,18 * * ?")),
                  () -> assertEquals("Every minute between 14:00 and 14:05, every day", CronExpressionDescriptor.getDescription("0 0-5 14 * * ?")),
                  () -> assertEquals("At 10 and 44 minutes past the hour, at 14:00, every day, only on Wednesday, only in March", CronExpressionDescriptor.getDescription("0 10,44 14 ? 3 WED")),
                  () -> assertEquals("At 10:15, every day, Monday through Friday", CronExpressionDescriptor.getDescription("0 15 10 ? * MON-FRI")),
                  () -> assertEquals("At 10:15, on day 15 of the month", CronExpressionDescriptor.getDescription("0 15 10 15 * ?")),
                  () -> assertEquals("At 10:15, on the last day of the month", CronExpressionDescriptor.getDescription("0 15 10 L * ?")),
                  () -> assertEquals("At 10:15, 2 days before the last day of the month", CronExpressionDescriptor.getDescription("0 15 10 L-2 * ?")),
                  () -> assertEquals("At 10:15, every day, on the last Friday of the month", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L")),
                  () -> assertEquals("At 10:15, every day, on the last Friday of the month, 2002 through 2005", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L 2002-2005")),
                  () -> assertEquals("At 10:15, every day, on the third Friday of the month", CronExpressionDescriptor.getDescription("0 15 10 ? * 6#3")),
                  () -> assertEquals("At 12:00, every 5 days", CronExpressionDescriptor.getDescription("0 0 12 1/5 * ?")),
                  () -> assertEquals("At 11:11, on day 11 of the month, only in November", CronExpressionDescriptor.getDescription("0 11 11 11 11 ?"))
        );
    }
}