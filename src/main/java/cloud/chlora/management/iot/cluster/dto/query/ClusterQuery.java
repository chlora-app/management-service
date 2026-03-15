package cloud.chlora.management.iot.cluster.dto.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClusterQuery {

    private int page = 1;
    private int size = 10;
    private String search;
    private String sort = "clusterId";
    private String order;
}
