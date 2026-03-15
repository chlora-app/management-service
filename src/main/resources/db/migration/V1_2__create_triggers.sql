-- =========================================
-- FUNCTION: generate user_id
-- =========================================
CREATE OR REPLACE FUNCTION generate_user_id() RETURNS TRIGGER AS $$
    BEGIN
        IF NEW.user_id IS NULL THEN
            NEW.user_id := 'U' || LPAD(nextval('user_seq')::TEXT, 5, '0');
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

-- =========================================
-- FUNCTION: generate cluster_id
-- =========================================
CREATE OR REPLACE FUNCTION generate_cluster_id() RETURNS TRIGGER AS $$
    BEGIN
        IF NEW.cluster_id IS NULL THEN
            NEW.cluster_id := 'CL-' || LPAD(nextval('cluster_seq')::TEXT, 5, '0');
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

-- =========================================
-- FUNCTION: generate device_id
-- =========================================
CREATE OR REPLACE FUNCTION generate_device_id() RETURNS TRIGGER AS $$
    BEGIN
        IF NEW.device_id IS NULL THEN
            NEW.device_id := 'DVC-' || LPAD(nextval('device_seq')::TEXT, 5, '0');
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

-- =========================================
-- TRIGGERS
-- =========================================
CREATE TRIGGER trg_generate_device_id
    BEFORE INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION generate_user_id();

CREATE TRIGGER trg_generate_cluster_id
    BEFORE INSERT ON clusters
    FOR EACH ROW
    EXECUTE FUNCTION generate_cluster_id();

CREATE TRIGGER trg_generate_device_id
    BEFORE INSERT ON devices
    FOR EACH ROW
    EXECUTE FUNCTION generate_device_id();