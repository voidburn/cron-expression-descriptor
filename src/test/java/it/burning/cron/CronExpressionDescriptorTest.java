package it.burning.cron;

import it.burning.cron.CronExpressionParser.Options;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CronExpressionDescriptorTest {
    // Default options
    static final Options DEFAULT_OPTIONS = new Options();

    @Test
    void setOptions() {
        final CronExpressionDescriptor descriptor = new CronExpressionDescriptor("0 0 12 * * ?", DEFAULT_OPTIONS);
        assertEquals(DEFAULT_OPTIONS, descriptor.getOptions());

        // Set new options
        final Options newOptions = new Options() {{
            setLocale("it");
            setUse24HourTimeFormat(false);
        }};

        descriptor.setOptions(newOptions);
        assertAll("All options should have been set",
                  () -> assertEquals(newOptions, descriptor.getOptions()),
                  () -> assertFalse(descriptor.isUse24HourTimeFormat()),
                  () -> assertEquals("it", descriptor.getLocale().getLanguage()),
                  () -> assertEquals("it", descriptor.getLocalization().getLocale().getLanguage())
        );


    }

    @Test
    void getOptions() {
        final CronExpressionDescriptor descriptor = new CronExpressionDescriptor("0 0 12 * * ?", DEFAULT_OPTIONS);
        assertEquals(DEFAULT_OPTIONS, descriptor.getOptions());
    }

    @Test
    void setDescription() {
        // Build a new reusable descriptor
        final CronExpressionDescriptor descriptor = new CronExpressionDescriptor();

        // Attempt to parse without setting an expression
        assertThrows(IllegalArgumentException.class, descriptor::getDescription, "Expected getDescription() to throw, but it didn't");

        // Set an expression and parse it
        descriptor.setExpression("0 0 12 * * ?", DEFAULT_OPTIONS);
        final String description = descriptor.getDescription();
        assertEquals("At 12:00", description);

        // Set a new expression, options and parse it
        descriptor.setExpression("0 0 12 * * ?", new Options() {{
            setLocale(new Locale("it"));
        }});
        final String localizedDescription = descriptor.getDescription();
        assertEquals("Alle 12:00", localizedDescription);

        // Set an invalid expression
        assertThrows(IllegalArgumentException.class, () -> descriptor.setExpression("", DEFAULT_OPTIONS), "Expected setDescription() to throw, but it didn't");

        // Set invalid options
        assertThrows(IllegalArgumentException.class, () -> descriptor.setExpression("0 0 12 * * ?", null), "Expected setDescription() to throw, but it didn't");
    }

    @Test
    void getDescription() {
        // Test examples taken from (http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#examples)
        assertEquals("At 12:00", CronExpressionDescriptor.getDescription("0 0 12 * * ?", DEFAULT_OPTIONS));
        assertEquals("At 10:15", CronExpressionDescriptor.getDescription("0 15 10 ? * *", DEFAULT_OPTIONS));
        assertEquals("At 10:15", CronExpressionDescriptor.getDescription("0 15 10 * * ?", DEFAULT_OPTIONS));
        assertEquals("At 10:15", CronExpressionDescriptor.getDescription("0 15 10 * * ? *", DEFAULT_OPTIONS));
        assertEquals("At 10:15, only in 2005", CronExpressionDescriptor.getDescription("0 15 10 * * ? 2005", DEFAULT_OPTIONS));
        assertEquals("Every minute, between 14:00 and 14:59", CronExpressionDescriptor.getDescription("0 * 14 * * ?", DEFAULT_OPTIONS));
        assertEquals("Every 5 minutes, between 14:00 and 14:59", CronExpressionDescriptor.getDescription("0 0/5 14 * * ?", DEFAULT_OPTIONS));
        assertEquals("Every 5 minutes, at 14:00 and 18:00", CronExpressionDescriptor.getDescription("0 0/5 14,18 * * ?", DEFAULT_OPTIONS));
        assertEquals("Every minute between 14:00 and 14:05", CronExpressionDescriptor.getDescription("0 0-5 14 * * ?", DEFAULT_OPTIONS));
        assertEquals("Every 10 minutes, Monday through Friday", CronExpressionDescriptor.getDescription("0/10 * ? * MON-FRI *", DEFAULT_OPTIONS));
        assertEquals("At 10 and 44 minutes past the hour, at 14:00, only on Wednesday, only in March", CronExpressionDescriptor.getDescription("0 10,44 14 ? 3 WED", DEFAULT_OPTIONS));
        assertEquals("At 10:15, Monday through Friday", CronExpressionDescriptor.getDescription("0 15 10 ? * MON-FRI", DEFAULT_OPTIONS));
        assertEquals("At 10:15, on day 15 of the month", CronExpressionDescriptor.getDescription("0 15 10 15 * ?", DEFAULT_OPTIONS));
        assertEquals("At 10:15, on the last day of the month", CronExpressionDescriptor.getDescription("0 15 10 L * ?", DEFAULT_OPTIONS));
        assertEquals("At 10:15, 2 days before the last day of the month", CronExpressionDescriptor.getDescription("0 15 10 L-2 * ?", DEFAULT_OPTIONS));
        assertEquals("At 10:15, on the last Friday of the month", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L", DEFAULT_OPTIONS));
        assertEquals("At 10:15, on the last Friday of the month, 2002 through 2005", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L 2002-2005", DEFAULT_OPTIONS));
        assertEquals("At 10:15, on the third Friday of the month", CronExpressionDescriptor.getDescription("0 15 10 ? * 6#3", DEFAULT_OPTIONS));
        assertEquals("At 15:00, on day 10 of the month, only on Saturday", CronExpressionDescriptor.getDescription("0 15 10 * 6", DEFAULT_OPTIONS));
        assertEquals("At 12:00, every 5 days", CronExpressionDescriptor.getDescription("0 0 12 1/5 * ?", DEFAULT_OPTIONS));
        assertEquals("At 11:11, on day 11 of the month, only in November", CronExpressionDescriptor.getDescription("0 11 11 11 11 ?", DEFAULT_OPTIONS));
        assertEquals("Every 10 minutes, starting at 5 minutes past the hour", CronExpressionDescriptor.getDescription("5/10 * * * *", DEFAULT_OPTIONS));
        assertEquals("Every 10 hours, starting at 01:00", CronExpressionDescriptor.getDescription("0 1/10 * * *", DEFAULT_OPTIONS));
    }

    @Test
    void setDefaultLocale() {
        // Test setting a default locale
        CronExpressionDescriptor.setDefaultLocale("it");
        assertEquals("Alle 12:00", CronExpressionDescriptor.getDescription("0 0 12 * * ?"));
        assertEquals("Alle 10:15", CronExpressionDescriptor.getDescription("0 15 10 ? * *"));
        assertEquals("Alle 10:15", CronExpressionDescriptor.getDescription("0 15 10 * * ?"));
        assertEquals("Alle 10:15", CronExpressionDescriptor.getDescription("0 15 10 * * ? *"));
        assertEquals("Alle 10:15, solo nel 2005", CronExpressionDescriptor.getDescription("0 15 10 * * ? 2005"));
        assertEquals("Ogni minuto, tra le 14:00 e le 14:59", CronExpressionDescriptor.getDescription("0 * 14 * * ?"));
        assertEquals("Ogni 5 minuti, tra le 14:00 e le 14:59", CronExpressionDescriptor.getDescription("0 0/5 14 * * ?"));
        assertEquals("Ogni 5 minuti, alle 14:00 e 18:00", CronExpressionDescriptor.getDescription("0 0/5 14,18 * * ?"));
        assertEquals("Ogni minuto tra le 14:00 e le 14:05", CronExpressionDescriptor.getDescription("0 0-5 14 * * ?"));
        assertEquals("Al minuto 10 e 44 passata l'ora, alle 14:00, solo il Mercoled\u00EC, solo in Marzo", CronExpressionDescriptor.getDescription("0 10,44 14 ? 3 WED"));
        assertEquals("Alle 10:15, dal Luned\u00EC al Venerd\u00EC", CronExpressionDescriptor.getDescription("0 15 10 ? * MON-FRI"));
        assertEquals("Alle 10:15, il giorno 15 del mese", CronExpressionDescriptor.getDescription("0 15 10 15 * ?"));
        assertEquals("Alle 10:15, l'ultimo giorno del mese", CronExpressionDescriptor.getDescription("0 15 10 L * ?"));
        assertEquals("Alle 10:15, 2 giorni prima dell'ultimo giorno del mese", CronExpressionDescriptor.getDescription("0 15 10 L-2 * ?"));
        assertEquals("Alle 10:15, l'ultimo Venerd\u00EC del mese", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L"));
        assertEquals("Alle 10:15, l'ultimo Venerd\u00EC del mese, dal 2002 al 2005", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L 2002-2005"));
        assertEquals("Alle 10:15, il terzo Venerd\u00EC del mese", CronExpressionDescriptor.getDescription("0 15 10 ? * 6#3"));
        assertEquals("Alle 15:00, il giorno 10 del mese, solo il Sabato", CronExpressionDescriptor.getDescription("0 15 10 * 6"));
        assertEquals("Alle 12:00, ogni 5 giorni", CronExpressionDescriptor.getDescription("0 0 12 1/5 * ?"));
        assertEquals("Alle 11:11, il giorno 11 del mese, solo in Novembre", CronExpressionDescriptor.getDescription("0 11 11 11 11 ?"));
        assertEquals("Ogni 10 minuti, a partire al minuto 5 passata l'ora", CronExpressionDescriptor.getDescription("5/10 * * * *"));
        assertEquals("Ogni 10 ore, a partire alle 01:00", CronExpressionDescriptor.getDescription("0 1/10 * * *"));

        // Test resetting default locale
        CronExpressionDescriptor.setDefaultLocale();
        assertEquals("At 12:00", CronExpressionDescriptor.getDescription("0 0 12 * * ?"));
        assertEquals("At 10:15", CronExpressionDescriptor.getDescription("0 15 10 ? * *"));
        assertEquals("At 10:15", CronExpressionDescriptor.getDescription("0 15 10 * * ?"));
        assertEquals("At 10:15", CronExpressionDescriptor.getDescription("0 15 10 * * ? *"));
        assertEquals("At 10:15, only in 2005", CronExpressionDescriptor.getDescription("0 15 10 * * ? 2005"));
        assertEquals("Every minute, between 14:00 and 14:59", CronExpressionDescriptor.getDescription("0 * 14 * * ?"));
        assertEquals("Every 5 minutes, between 14:00 and 14:59", CronExpressionDescriptor.getDescription("0 0/5 14 * * ?"));
        assertEquals("Every 5 minutes, at 14:00 and 18:00", CronExpressionDescriptor.getDescription("0 0/5 14,18 * * ?"));
        assertEquals("Every minute between 14:00 and 14:05", CronExpressionDescriptor.getDescription("0 0-5 14 * * ?"));
        assertEquals("At 10 and 44 minutes past the hour, at 14:00, only on Wednesday, only in March", CronExpressionDescriptor.getDescription("0 10,44 14 ? 3 WED"));
        assertEquals("At 10:15, Monday through Friday", CronExpressionDescriptor.getDescription("0 15 10 ? * MON-FRI"));
        assertEquals("At 10:15, on day 15 of the month", CronExpressionDescriptor.getDescription("0 15 10 15 * ?"));
        assertEquals("At 10:15, on the last day of the month", CronExpressionDescriptor.getDescription("0 15 10 L * ?"));
        assertEquals("At 10:15, 2 days before the last day of the month", CronExpressionDescriptor.getDescription("0 15 10 L-2 * ?"));
        assertEquals("At 10:15, on the last Friday of the month", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L"));
        assertEquals("At 10:15, on the last Friday of the month, 2002 through 2005", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L 2002-2005"));
        assertEquals("At 10:15, on the third Friday of the month", CronExpressionDescriptor.getDescription("0 15 10 ? * 6#3"));
        assertEquals("At 15:00, on day 10 of the month, only on Saturday", CronExpressionDescriptor.getDescription("0 15 10 * 6"));
        assertEquals("At 12:00, every 5 days", CronExpressionDescriptor.getDescription("0 0 12 1/5 * ?"));
        assertEquals("At 11:11, on day 11 of the month, only in November", CronExpressionDescriptor.getDescription("0 11 11 11 11 ?"));
        assertEquals("Every 10 minutes, starting at 5 minutes past the hour", CronExpressionDescriptor.getDescription("5/10 * * * *"));
        assertEquals("Every 10 hours, starting at 01:00", CronExpressionDescriptor.getDescription("0 1/10 * * *"));

        CronExpressionDescriptor.setDefaultLocale(Locale.forLanguageTag("pt"));
        assertEquals("Às 12:00", CronExpressionDescriptor.getDescription("0 0 12 * * ?"));
        assertEquals("Às 10:15", CronExpressionDescriptor.getDescription("0 15 10 ? * *"));
        assertEquals("Às 10:15", CronExpressionDescriptor.getDescription("0 15 10 * * ?"));
        assertEquals("Às 10:15", CronExpressionDescriptor.getDescription("0 15 10 * * ? *"));
        assertEquals("Às 10:15, somente em 2005", CronExpressionDescriptor.getDescription("0 15 10 * * ? 2005"));
        assertEquals("A cada minuto, entre 14:00 e 14:59", CronExpressionDescriptor.getDescription("0 * 14 * * ?"));
        assertEquals("A cada 5 minutos, entre 14:00 e 14:59", CronExpressionDescriptor.getDescription("0 0/5 14 * * ?"));
        assertEquals("A cada 5 minutos, Às 14:00 e 18:00", CronExpressionDescriptor.getDescription("0 0/5 14,18 * * ?"));
        assertEquals("A cada minuto entre 14:00 e 14:05", CronExpressionDescriptor.getDescription("0 0-5 14 * * ?"));
        assertEquals("Aos 10 e 44 minutos da hora, Às 14:00, somente de Quarta-feira, somente em Março", CronExpressionDescriptor.getDescription("0 10,44 14 ? 3 WED"));
        assertEquals("Às 10:15, de Segunda-feira a Sexta-feira", CronExpressionDescriptor.getDescription("0 15 10 ? * MON-FRI"));
        assertEquals("Às 10:15, no dia 15 do mês", CronExpressionDescriptor.getDescription("0 15 10 15 * ?"));
        assertEquals("Às 10:15, no último dia do mês", CronExpressionDescriptor.getDescription("0 15 10 L * ?"));
        assertEquals("Às 10:15, 2 dias antes do último dia do mês", CronExpressionDescriptor.getDescription("0 15 10 L-2 * ?"));
        assertEquals("Às 10:15, na última Sexta-feira do mês", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L"));
        assertEquals("Às 10:15, na última Sexta-feira do mês, de 2002 a 2005", CronExpressionDescriptor.getDescription("0 15 10 ? * 6L 2002-2005"));
        assertEquals("Às 10:15, on the terceira Sexta-feira do mês", CronExpressionDescriptor.getDescription("0 15 10 ? * 6#3"));
        assertEquals("Às 15:00, no dia 10 do mês, somente de Sábado", CronExpressionDescriptor.getDescription("0 15 10 * 6"));
        assertEquals("Às 12:00, a cada 5 dias", CronExpressionDescriptor.getDescription("0 0 12 1/5 * ?"));
        assertEquals("Às 11:11, no dia 11 do mês, somente em Novembro", CronExpressionDescriptor.getDescription("0 11 11 11 11 ?"));
        assertEquals("A cada 10 minutos, iniciando aos 5 minutos da hora", CronExpressionDescriptor.getDescription("5/10 * * * *"));
        assertEquals("A cada 10 horas, iniciando Às 01:00", CronExpressionDescriptor.getDescription("0 1/10 * * *"));

    }
}