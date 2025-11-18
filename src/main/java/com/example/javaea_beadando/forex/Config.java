package com.example.javaea_beadando.forex;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountID;

public class Config {


    private Config() {
    }

    // FONTOS: Cseréld le ezeket az értékeket a saját OANDA API adataidra!
    // Regisztrálj itt: https://www.oanda.com/forex-trading/
    public static final String URL = "https://api-fxpractice.oanda.com";
    public static final String TOKEN = "cd93c87a32ab277e7c01b13ecc53a5af-f1eb1f501d623e6604284ea4d9c46575";
    public static final AccountID ACCOUNTID = new AccountID("101-004-37392252-001");

    private static Context context;

    public static Context getContext() {
        if (context == null) {
            try {
                context = new ContextBuilder(URL)
                        .setToken(TOKEN)
                        .setApplication("ForexApp")
                        .build();
            } catch (Exception e) {
                System.err.println("Hiba a Context létrehozása során: " + e.getMessage());
                throw new RuntimeException("Nem sikerült létrehozni az OANDA API kapcsolatot. Ellenőrizd a TOKEN és ACCOUNTID értékeket!", e);
            }
        }
        return context;
    }
}

