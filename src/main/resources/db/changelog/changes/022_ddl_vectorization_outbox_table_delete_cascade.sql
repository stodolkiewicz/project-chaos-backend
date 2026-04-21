--changeset stodo:40 context:dev,prod
ALTER TABLE vectorization_outbox
DROP CONSTRAINT vectorization_outbox_attachment_id_fkey,
  ADD CONSTRAINT vectorization_outbox_attachment_id_fkey
    FOREIGN KEY (attachment_id) REFERENCES attachments(id) ON DELETE CASCADE;
--rollback ALTER TABLE vectorization_outbox DROP CONSTRAINT vectorization_outbox_attachment_id_fkey;
--rollback ALTER TABLE vectorization_outbox ADD CONSTRAINT vectorization_outbox_attachment_id_fkey FOREIGN KEY (attachment_id) REFERENCES attachments(id);