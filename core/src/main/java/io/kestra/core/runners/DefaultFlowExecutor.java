package io.kestra.core.runners;

import io.kestra.core.models.flows.FlowWithSource;
import io.kestra.core.repositories.FlowRepositoryInterface;
import io.kestra.core.services.FlowListenersInterface;
import jakarta.inject.Singleton;
import java.util.Objects;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Singleton
public class DefaultFlowExecutor implements FlowExecutorInterface {
    private final FlowRepositoryInterface flowRepository;

    @Setter
    private List<FlowWithSource> allFlows;

    public DefaultFlowExecutor(FlowListenersInterface flowListeners, FlowRepositoryInterface flowRepository) {
        this.flowRepository = flowRepository;

        flowListeners.listen(flows -> this.allFlows = flows);
    }

    @Override
    public Collection<FlowWithSource> allLastVersion() {
        return this.allFlows;
    }

    @Override
    public Optional<FlowWithSource> findById(String tenantId, String namespace, String id, Optional<Integer> revision) {
        Optional<FlowWithSource> find = this.allFlows
            .stream()
            .filter(flow -> ((flow.getTenantId() == null && tenantId == null) || Objects.equals(flow.getTenantId(), tenantId)) &&
                flow.getNamespace().equals(namespace) &&
                flow.getId().equals(id) &&
                (revision.isEmpty() || revision.get().equals(flow.getRevision()))
            )
            .findFirst();

        if (find.isPresent()) {
            return find;
        } else {
            return flowRepository.findByIdWithSource(tenantId, namespace, id, revision);
        }
    }

    @Override
    public Boolean isReady() {
        return true;
    }
}
