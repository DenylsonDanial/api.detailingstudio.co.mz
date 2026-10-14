package mz.co.amaterasu.gestao.util;

public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    /**
     * Nome da base de dados master. Definido no arranque a partir de {@code app.db.master.name}
     * por {@link mz.co.amaterasu.gestao.base.config.TenantContextInitializer}.
     */
    private static volatile String masterDb = "ds_master";

    public static void configureMasterDb(String name) {
        masterDb = name;
    }

    public static String getMasterDb() {
        return masterDb;
    }

    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setMasterTenant() {
        setCurrentTenant(masterDb);
    }

    public static boolean isMaster() {
        return masterDb.equals(getCurrentTenant());
    }

    public static void clear() {
        currentTenant.remove();
    }
}
