--DROP SEQUENCE users_seq;
CREATE SEQUENCE users_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

--DROP TABLE users;
CREATE TABLE users (
    user_id        NUMBER
        CONSTRAINT users_pk PRIMARY KEY,

    name           VARCHAR2(100) NOT NULL,

    email          VARCHAR2(150) NOT NULL
        CONSTRAINT users_email_uk UNIQUE,

    password_hash  VARCHAR2(255) NOT NULL,

    image_path     VARCHAR2(255)

);

CREATE OR REPLACE TRIGGER users_bi_trg
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    IF :NEW.user_id IS NULL THEN
        SELECT users_seq.NEXTVAL
        INTO   :NEW.user_id
        FROM   dual;
    END IF;
END;
/

--DROP TABLE users_friends;
CREATE TABLE users_friends (
    requester_id  NUMBER NOT NULL,
    receiver_id   NUMBER NOT NULL,

    status         VARCHAR2(10) NOT NULL,

    CONSTRAINT users_friends_pk
        PRIMARY KEY (requester_id, receiver_id),

    CONSTRAINT users_friends_requester_fk
        FOREIGN KEY (requester_id)
        REFERENCES users (user_id),

    CONSTRAINT users_friends_receiver_fk
        FOREIGN KEY (receiver_id)
        REFERENCES users (user_id),

    CONSTRAINT users_friends_status_ck
        CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED')),

    CONSTRAINT users_friends_no_self_ck
        CHECK (requester_id <> receiver_id)
);

--DROP SEQUENCE wishes_seq;
CREATE SEQUENCE wishes_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

--DROP TABLE wishes;
CREATE TABLE wishes (
    wish_id       NUMBER
        CONSTRAINT wishes_pk PRIMARY KEY,

    user_id       NUMBER NOT NULL,

    name          VARCHAR2(150) NOT NULL,

    description   VARCHAR2(1000),

    price         NUMBER(10,2)
        CONSTRAINT wishes_price_ck CHECK (price >= 0),

    image_path    VARCHAR2(255),

    is_completed  CHAR(1) DEFAULT 'N' NOT NULL,

    CONSTRAINT wishes_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),

    CONSTRAINT wishes_completed_ck
        CHECK (is_completed IN ('Y', 'N'))
);

CREATE OR REPLACE TRIGGER wishes_bi_trg
BEFORE INSERT ON wishes
FOR EACH ROW
BEGIN
    IF :NEW.wish_id IS NULL THEN
        SELECT wishes_seq.NEXTVAL
        INTO   :NEW.wish_id
        FROM   dual;
    END IF;
END;
/

--DROP SEQUENCE users_contributions_seq;
CREATE SEQUENCE users_contributions_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;


--DROP TABLE users_contributions;
CREATE TABLE users_contributions (
    contribution_id   NUMBER
        CONSTRAINT users_contributions_pk PRIMARY KEY,

    user_id           NUMBER NOT NULL,

    wish_id           NUMBER NOT NULL,

    amount            NUMBER(10,2) NOT NULL,

    CONSTRAINT users_contributions_user_fk
        FOREIGN KEY (user_id)
        REFERENCES users (user_id),

    CONSTRAINT users_contributions_wish_fk
        FOREIGN KEY (wish_id)
        REFERENCES wishes (wish_id),

    CONSTRAINT users_contributions_amount_ck
        CHECK (amount > 0)
);


CREATE OR REPLACE TRIGGER users_contributions_bi_trg
BEFORE INSERT ON users_contributions
FOR EACH ROW
BEGIN
    IF :NEW.contribution_id IS NULL THEN
        SELECT users_contributions_seq.NEXTVAL
        INTO   :NEW.contribution_id
        FROM   dual;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER auto_complete_wish_trg
FOR INSERT ON users_contributions
COMPOUND TRIGGER

    -- ?? Global shared memory
    TYPE t_wish_ids IS TABLE OF NUMBER INDEX BY PLS_INTEGER;
    g_wish_ids t_wish_ids;
    g_idx PLS_INTEGER := 0;

    
    AFTER EACH ROW IS
    BEGIN
        g_idx := g_idx + 1;
        g_wish_ids(g_idx) := :NEW.wish_id;
    END AFTER EACH ROW;

    AFTER STATEMENT IS
    BEGIN
        DECLARE
            v_total NUMBER;
            v_price NUMBER;
        BEGIN
            FOR i IN 1 .. g_wish_ids.COUNT LOOP

                SELECT NVL(SUM(u.amount), 0), w.price
                INTO v_total, v_price
                FROM users_contributions u
                JOIN wishes w ON u.wish_id = w.wish_id
                WHERE u.wish_id = g_wish_ids(i)
                GROUP BY w.price;

                IF v_total >= v_price THEN
                    UPDATE wishes
                    SET is_completed = 'Y'
                    WHERE wish_id = g_wish_ids(i)
                      AND is_completed = 'N';
                END IF;

            END LOOP;
        END;
    END AFTER STATEMENT;

END auto_complete_wish_trg;
/



--DROP SEQUENCE notifications_seq;
CREATE SEQUENCE notifications_seq
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

--DROP TABLE notifications;
CREATE TABLE notifications (
    notification_id   NUMBER
        CONSTRAINT notifications_pk PRIMARY KEY,

    receiver_id       NUMBER NOT NULL,

    wish_id           NUMBER,

    type              VARCHAR2(20) NOT NULL,

    message           VARCHAR2(1000) NOT NULL,

    is_read            CHAR(1) DEFAULT 'N' NOT NULL,

    CONSTRAINT notifications_receiver_fk
        FOREIGN KEY (receiver_id)
        REFERENCES users (user_id),

    CONSTRAINT notifications_wish_fk
        FOREIGN KEY (wish_id)
        REFERENCES wishes (wish_id),

    CONSTRAINT notifications_type_ck
        CHECK (type IN ('WISH_COMPLETED', 'WISH_BOUGHT')),

    CONSTRAINT notifications_is_read_ck
        CHECK (is_read IN ('Y', 'N'))
);

CREATE OR REPLACE TRIGGER wishes_notifications_trg
AFTER UPDATE OF is_completed ON wishes
FOR EACH ROW
WHEN (OLD.is_completed = 'N' AND NEW.is_completed = 'Y')
DECLARE

    CURSOR c_contributors IS
        SELECT DISTINCT user_id
        FROM users_contributions
        WHERE wish_id = :NEW.wish_id
          AND user_id != :NEW.user_id; 
BEGIN

    INSERT INTO notifications (
        notification_id,
        receiver_id,
        wish_id,
        type,
        message,
        is_read
    )
    VALUES (
        notifications_seq.NEXTVAL,
        :NEW.user_id,
        :NEW.wish_id,
        'WISH_COMPLETED',
        'Your wish "' || :NEW.name || '" has been fully funded!!',
        'N'
    );


    FOR r IN c_contributors LOOP
        INSERT INTO notifications (
            notification_id,
            receiver_id,
            wish_id,
            type,
            message,
            is_read
        )
        VALUES (
            notifications_seq.NEXTVAL,
            r.user_id,
            :NEW.wish_id,
            'WISH_BOUGHT',
            'The wish "' || :NEW.name || '" you contributed to has been completed!',
            'N'
        );
    END LOOP;
END;
/
