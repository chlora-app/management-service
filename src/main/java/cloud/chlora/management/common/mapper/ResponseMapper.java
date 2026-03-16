package cloud.chlora.management.common.mapper;

import cloud.chlora.management.iot.cluster.domain.Cluster;
import cloud.chlora.management.iot.cluster.dto.response.*;
import cloud.chlora.management.iot.device.domain.Device;
import cloud.chlora.management.iot.device.dto.response.*;
import cloud.chlora.management.user.domain.entity.User;
import cloud.chlora.management.user.dto.response.UserCreateResponse;
import cloud.chlora.management.user.dto.response.UserDeletedResponse;
import cloud.chlora.management.user.dto.response.UserGetResponse;
import cloud.chlora.management.user.dto.response.UserUpdateResponse;

import java.util.List;

public class ResponseMapper {

    public static class UserMapper {
        public static UserGetResponse toGetResponse(User user) {
            return new UserGetResponse(
                    user.getUserId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        }

        public static UserCreateResponse toCreateResponse(User user) {
            return new UserCreateResponse(
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getCreatedAt()
            );
        }

        public static UserUpdateResponse toUpdateResponse(User user) {
            return new UserUpdateResponse(
                    user.getUserId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getUpdatedAt()
            );
        }

        public static UserDeletedResponse toDeletedResponse(cloud.chlora.management.user.domain.entity.User user) {
            return new UserDeletedResponse(
                    user.getUserId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole(),
                    user.getDeletedAt()
            );
        }
    }

    public static class DeviceMapper {
        public static PagedDeviceResponse toPagedResponse(
                long countData,
                int page,
                int size,
                int totalPages,
                List<PagedDeviceList> devices
        ) {

            return new PagedDeviceResponse(countData, page, size, totalPages, devices);
        }

        public static DeviceListResponse toListResponse(Device device) {
            return new DeviceListResponse(
                    device.getDeviceId(),
                    device.getDeviceName(),
                    device.getDeviceType(),
                    device.getStatus()
            );
        }

        public static DeviceGetResponse toGetResponse(Device device) {
            return new DeviceGetResponse(
                    device.getDeviceId(),
                    device.getDeviceName(),
                    device.getDeviceType(),
                    device.getClusterId(),
                    device.getStatus(),
                    device.getCreatedAt(),
                    device.getUpdatedAt()
            );
        }

        public static DeviceCreateResponse toCreateResponse(Device device) {
            return new DeviceCreateResponse(
                    device.getDeviceId(),
                    device.getDeviceName(),
                    device.getDeviceType(),
                    device.getClusterId(),
                    device.getStatus(),
                    device.getCreatedAt()
            );
        }

        public static DeviceUpdateResponse toUpdateResponse(Device device) {
            return new DeviceUpdateResponse(
                    device.getDeviceId(),
                    device.getDeviceName(),
                    device.getDeviceType(),
                    device.getStatus(),
                    device.getClusterId(),
                    device.getUpdatedAt()
            );
        }
    }

    public static class ClusterMapper {
        public static PagedClusterResponse toPagedResponse(
                long countData,
                int page,
                int size,
                int totalPages,
                List<ClusterListResponse> clusters
        ) {
            return new PagedClusterResponse(countData, page, size, totalPages, clusters);
        }

        public static ClusterGetResponse toGetResponse(Cluster cluster, long totalDevices, List<DeviceListResponse> devices) {
            return new ClusterGetResponse(
                    cluster.getClusterId(),
                    cluster.getClusterName(),
                    totalDevices,
                    devices
            );
        }

        public static ClusterCreateResponse toCreateResponse(Cluster cluster) {
            return new ClusterCreateResponse(
                    cluster.getClusterId(),
                    cluster.getClusterName(),
                    cluster.getCreatedAt()
            );
        }

        public static ClusterUpdateResponse toUpdateResponse(Cluster cluster) {
            return new ClusterUpdateResponse(
                    cluster.getClusterId(),
                    cluster.getClusterName(),
                    cluster.getUpdatedAt()
            );
        }
    }
}
