DROP INDEX qidx;
DROP INDEX ridx;
DROP INDEX sidx;
DROP INDEX midx;
DROP INDEX tidx;
DROP TABLE messages;
DROP TABLE metadata;
DROP TABLE clients;
DROP TABLE queues;

GRANT ALL ON metadata TO user_asl;
GRANT ALL ON queues TO user_asl;
GRANT ALL ON clients TO user_asl;
GRANT ALL ON messages TO user_asl;
GRANT ALL ON clients_client_id_seq TO user_asl;
GRANT ALL ON metadata_message_id_seq TO user_asl;
GRANT ALL ON queues_queue_id_seq TO user_asl;



CREATE TABLE queues(
    queue_id SERIAL PRIMARY KEY
    );

CREATE TABLE clients(
    client_id SERIAL PRIMARY KEY,
    flag boolean DEFAULT TRUE
    );

CREATE TABLE metadata(
    message_id SERIAL PRIMARY KEY,
    timestamp timestamp DEFAULT NOW(),
    queue_id int REFERENCES queues(queue_id),
    sender_id int REFERENCES clients(client_id),
    receiver_id int REFERENCES clients(client_id)
    );

CREATE TABLE messages(
    message_id int REFERENCES metadata(message_id),
    payload varchar(3000)
    );
 
CREATE INDEX qidx ON metadata (queue_id);
CREATE INDEX ridx ON metadata (receiver_id);
CREATE INDEX sidx ON metadata (sender_id);
CREATE INDEX midx ON messages (message_id);
CREATE INDEX tidx ON metadata (timestamp);

INSERT INTO clients (client_id, flag) VALUES (0, true);   


