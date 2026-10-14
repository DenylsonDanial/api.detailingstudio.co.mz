package mz.co.amaterasu.gestao.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import mz.co.amaterasu.gestao.base.enums.RecordStatus;
import mz.co.amaterasu.gestao.client.entity.Client;
import mz.co.amaterasu.gestao.client.entity.Vehicle;
import mz.co.amaterasu.gestao.client.enums.ClientType;
import mz.co.amaterasu.gestao.client.enums.VehicleCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Pedidos e respostas dos modulos de clientes e viaturas. */
public final class ClientDTOs {

    private ClientDTOs() {
    }

    @Data
    public static class ClientRequest {
        @NotBlank(message = "O nome é obrigatório")
        private String name;
        private ClientType clientType;
        private String phone;
        /** So o gestor (manage:credit) pode criar um cliente com conta corrente. */
        private Boolean creditAllowed;
        private String notes;
    }

    @Data
    public static class CreditRequest {
        @NotNull(message = "Indique se a conta corrente fica autorizada")
        private Boolean creditAllowed;
        private String reason;
    }

    @Data
    public static class ClientResponse {
        private Long id;
        private String name;
        private ClientType clientType;
        private String phone;
        private boolean creditAllowed;
        private String notes;
        private RecordStatus status;
        private LocalDateTime createdAt;
        private String createdBy;
        private List<VehicleResponse> vehicles = new ArrayList<>();
        private BigDecimal currentDebt;
        private BigDecimal totalServices;
        private BigDecimal totalReceived;

        public static ClientResponse from(Client client) {
            ClientResponse dto = new ClientResponse();
            dto.setId(client.getId());
            dto.setName(client.getName());
            dto.setClientType(client.getClientType());
            dto.setPhone(client.getPhone());
            dto.setCreditAllowed(client.isCreditAllowed());
            dto.setNotes(client.getNotes());
            dto.setStatus(client.getStatus());
            dto.setCreatedAt(client.getCreatedAt());
            dto.setCreatedBy(client.getCreatedBy());
            return dto;
        }
    }

    @Data
    public static class VehicleRequest {
        @NotNull(message = "O cliente é obrigatório")
        private Long clientId;
        @NotBlank(message = "A matrícula é obrigatória")
        private String plate;
        private String brand;
        private String model;
        @NotNull(message = "A categoria de preço é obrigatória")
        private VehicleCategory category;
        private String notes;
    }

    @Data
    public static class ReassignRequest {
        @NotNull(message = "Seleccione o novo proprietário")
        private Long clientId;
        @NotBlank(message = "Indique o motivo da reassociação")
        private String reason;
    }

    @Data
    public static class VehicleResponse {
        private Long id;
        private Long clientId;
        private String clientName;
        private String plate;
        private String brand;
        private String model;
        private VehicleCategory category;
        private String categoryLabel;
        private String description;
        private String notes;
        private RecordStatus status;

        public static VehicleResponse from(Vehicle vehicle) {
            VehicleResponse dto = new VehicleResponse();
            dto.setId(vehicle.getId());
            dto.setClientId(vehicle.getClient().getId());
            dto.setClientName(vehicle.getClient().getName());
            dto.setPlate(vehicle.getPlate());
            dto.setBrand(vehicle.getBrand());
            dto.setModel(vehicle.getModel());
            dto.setCategory(vehicle.getCategory());
            dto.setCategoryLabel(vehicle.getCategory().getLabel());
            dto.setDescription(vehicle.describe());
            dto.setNotes(vehicle.getNotes());
            dto.setStatus(vehicle.getStatus());
            return dto;
        }
    }
}
