import { format, parseISO } from "date-fns"
import { ptBR } from "date-fns/locale"
import { CalendarIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"

interface DatePickerProps {
  value?: string
  onChange: (value: string | undefined) => void
  placeholder?: string
}

export function DatePicker({ value, onChange, placeholder = "Selecione uma data" }: DatePickerProps) {
  const date = value ? parseISO(value) : undefined

  return (
    <Popover>
      <PopoverTrigger
        render={
          <Button
            type="button"
            variant="outline"
            data-empty={!date}
            className="w-full justify-start text-left font-normal data-[empty=true]:text-muted-foreground"
          />
        }
      >
        <CalendarIcon className="size-4" />
        {date ? format(date, "dd 'de' MMMM 'de' yyyy", { locale: ptBR }) : placeholder}
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0">
        <Calendar
          mode="single"
          selected={date}
          locale={ptBR}
          onSelect={(selected) => onChange(selected ? format(selected, "yyyy-MM-dd") : undefined)}
        />
      </PopoverContent>
    </Popover>
  )
}
